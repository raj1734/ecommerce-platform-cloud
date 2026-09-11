# Embedded MongoDB Setup Guide for Catalog Service

This guide explains how the Catalog Service uses **Embedded MongoDB (Flapdoodle)** for in-memory testing, similar to how Auth Service and Order Service use H2 in-memory database.

---

## 🎯 Overview

**Embedded MongoDB** provides an in-memory MongoDB instance that:
- ✅ **No installation required** - Works out of the box
- ✅ **Fast startup** - Perfect for local development and testing
- ✅ **Isolated** - Each service instance gets its own database
- ✅ **Temporary** - Data is cleared on service restart (same as H2)
- ✅ **Consistent with H2** - Same development experience across all services

---

## 📊 Database Configuration by Environment

| Environment | Profile | Database | Installation Required | Data Persistence |
|-------------|---------|----------|----------------------|------------------|
| **Local** | `local` | Embedded MongoDB (Flapdoodle) | ❌ No | ❌ Lost on restart |
| **Review** | `review` | Embedded MongoDB (Flapdoodle) | ❌ No | ❌ Lost on restart |
| **Development** | `dev` | MongoDB (Real) | ✅ Yes | ✅ Persistent |
| **SIT** | `sit` | MongoDB (Real) | ✅ Yes | ✅ Persistent |
| **QA** | `qa` | MongoDB (Real) | ✅ Yes | ✅ Persistent |
| **Pre-Prod** | `pp` | MongoDB (Real) | ✅ Yes | ✅ Persistent |
| **Production** | `prod` | MongoDB (Real) | ✅ Yes | ✅ Persistent |

---

## 🛠️ Implementation Details

### Maven Dependency Added

**File**: `catalog-service/pom.xml`

```xml
<!-- Embedded MongoDB (In-Memory) for Local/Testing -->
<dependency>
    <groupId>de.flapdoodle.embed</groupId>
    <artifactId>de.flapdoodle.embed.mongo.spring30x</artifactId>
    <version>4.11.0</version>
    <scope>runtime</scope>
</dependency>
```

**Key Points**:
- ✅ **Spring Boot 3.x compatible** - Uses `spring30x` artifact
- ✅ **Runtime scope** - Only included during runtime, not in production builds
- ✅ **Auto-configuration** - Spring Boot automatically detects and configures
- ✅ **Latest version** - 4.11.0 (as of implementation)

### Configuration for Local Profile

**File**: `config-repo/catalog-service/application-local.yml`

```yaml
# Catalog Service - Local Environment Configuration
# Uses Embedded MongoDB (Flapdoodle) for in-memory testing
# No MongoDB installation required - perfect for quick local development

spring:
  # Embedded MongoDB (Flapdoodle) - In-Memory
  data:
    mongodb:
      # No URI needed - Flapdoodle auto-configures embedded MongoDB
      # Default: mongodb://localhost:27017/test
      database: catalog_db
  
  # Embedded Redis is not available, using local Redis
  # For fully in-memory local setup, you can use embedded-redis dependency
  redis:
    host: localhost
    port: 6379
  
  cache:
    type: redis

logging:
  level:
    root: DEBUG
    com.ecommerce.catalog: TRACE
    org.springframework.data.mongodb: DEBUG
    de.flapdoodle.embed: INFO  # Embedded MongoDB logs
```

**Key Configuration**:
- ❌ **No `uri` property** - Flapdoodle auto-configures the connection
- ✅ **Database name** - Specified as `catalog_db`
- ✅ **Logging** - Embedded MongoDB logs at INFO level
- ⚠️ **Redis** - Still requires local Redis (or use embedded-redis dependency)

### Configuration for Review Profile

**File**: `config-repo/catalog-service/application-review.yml`

```yaml
# Catalog Service - Review Environment Configuration
# For code review and PR testing
# Uses Embedded MongoDB (Flapdoodle) for in-memory testing

spring:
  # Embedded MongoDB (Flapdoodle) - In-Memory
  data:
    mongodb:
      # No URI needed - Flapdoodle auto-configures embedded MongoDB
      database: catalog_db
  
  redis:
    host: localhost
    port: 6379
  
  cache:
    type: redis

logging:
  level:
    root: INFO
    com.ecommerce.catalog: DEBUG
    de.flapdoodle.embed: INFO  # Embedded MongoDB logs
```

---

## 🚀 How to Use

### Start Catalog Service with Embedded MongoDB (Default)

```bash
cd catalog-service
mvn spring-boot:run
```

**What Happens**:
1. Service starts with `local` profile (default)
2. Flapdoodle downloads MongoDB binaries (first run only, ~100MB)
3. Embedded MongoDB starts on a random available port
4. Service connects to embedded MongoDB automatically
5. Collections are created automatically
6. Service ready on port 8082

**Expected Logs**:
```
INFO --- [main] de.flapdoodle.embed.mongo.MongodExecutable : Starting mongod...
INFO --- [main] de.flapdoodle.embed.mongo.MongodProcess    : mongod started
INFO --- [main] org.mongodb.driver.cluster                 : Cluster created with settings {hosts=[localhost:xxxxx]}
INFO --- [main] c.e.catalog.CatalogServiceApplication      : Started CatalogServiceApplication in 8.5 seconds
```

### Start with Specific Profile

```bash
# Local (Embedded MongoDB)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Review (Embedded MongoDB)
mvn spring-boot:run -Dspring-boot.run.profiles=review

# Dev (Real MongoDB required)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# SIT (Real MongoDB required)
mvn spring-boot:run -Dspring-boot.run.profiles=sit
```

---

## 🧪 Testing Embedded MongoDB

### Create a Product

```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 1299.99,
    "stock": 50,
    "category": "Electronics"
  }'
```

**Expected Response**:
```json
{
  "id": "507f1f77bcf86cd799439011",
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 1299.99,
  "stock": 50,
  "category": "Electronics",
  "createdAt": "2026-08-06T13:30:00",
  "updatedAt": "2026-08-06T13:30:00"
}
```

### Get All Products

```bash
curl http://localhost:8082/api/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Search Products by Category

```bash
curl "http://localhost:8082/api/products?category=Electronics" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Verify Data is Temporary

1. Create some products
2. Restart catalog-service
3. Query products again - **all data is gone** (expected behavior)

---

## 📊 Comparison: Embedded vs Real MongoDB

| Feature | Embedded MongoDB (local/review) | Real MongoDB (dev/sit/qa/pp/prod) |
|---------|--------------------------------|-----------------------------------|
| **Installation** | ❌ Not required | ✅ Required |
| **Startup Time** | ⚠️ Slower (first run downloads binaries) | ✅ Fast (after installation) |
| **Data Persistence** | ❌ Lost on restart | ✅ Persistent |
| **Disk Space** | ~100MB (binaries cached) | Varies |
| **Port** | Random available port | Fixed port (27017) |
| **Configuration** | ✅ Auto-configured | ⚠️ Manual setup |
| **Production Ready** | ❌ No | ✅ Yes |
| **Development** | ✅ Perfect | ⚠️ Requires setup |
| **Testing** | ✅ Fast | ⚠️ Slower |
| **Isolation** | ✅ Each instance isolated | ⚠️ Shared database |

---

## ⚠️ Important Notes

### First Run Download

On the **first run**, Flapdoodle will download MongoDB binaries (~100MB):

```
INFO --- [main] de.flapdoodle.embed.process.store.Downloader : Downloading https://fastdl.mongodb.org/...
INFO --- [main] de.flapdoodle.embed.process.store.Downloader : Downloaded 100MB
```

**Solution**: Be patient on first startup (may take 2-5 minutes depending on internet speed)

**Subsequent runs**: Binaries are cached, startup is fast (~5-10 seconds)

### Data is Temporary

- ❌ **Data is lost on service restart** - Embedded MongoDB stores data in memory
- ❌ **Not for production** - Migrate to real MongoDB before deployment
- ✅ **Perfect for development** - Fast iteration and testing without database overhead

### Redis Still Required

Embedded MongoDB only replaces MongoDB, **not Redis**:

- ⚠️ **Redis required** for caching to work
- ✅ **Alternative**: Add `embedded-redis` dependency for fully in-memory setup
- ✅ **Or**: Disable caching for local profile (not recommended)

**To add embedded Redis** (optional):

```xml
<!-- Embedded Redis (In-Memory) for Local/Testing -->
<dependency>
    <groupId>it.ozimov</groupId>
    <artifactId>embedded-redis</artifactId>
    <version>0.7.3</version>
    <scope>runtime</scope>
</dependency>
```

---

## 🔄 Migrating to Real MongoDB (Future)

When you're ready for production or need persistent storage:

### Step 1: Install MongoDB

**Windows**:
```bash
# Download from https://www.mongodb.com/try/download/community
# Or use Chocolatey
choco install mongodb
```

**Linux**:
```bash
sudo apt-get install -y mongodb-org
sudo systemctl start mongod
```

**macOS**:
```bash
brew tap mongodb/brew
brew install mongodb-community
brew services start mongodb-community
```

### Step 2: Create Database

```bash
# Connect to MongoDB
mongo

# Create database
use catalog_db

# Create a test collection
db.products.insertOne({name: "Test Product"})

# Verify
db.products.find()
```

### Step 3: Update Configuration

**For dev profile** (`config-repo/catalog-service/application-dev.yml`):

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/catalog_db
      auto-index-creation: true
```

**For production** (`config-repo/catalog-service/application-prod.yml`):

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://${MONGO_USERNAME}:${MONGO_PASSWORD}@${MONGO_HOST}:27017/catalog_db?authSource=admin
      auto-index-creation: false  # Disable for production
```

### Step 4: Remove Embedded MongoDB Dependency (Optional)

For production builds, you can exclude embedded MongoDB:

```xml
<!-- Embedded MongoDB (In-Memory) for Local/Testing -->
<dependency>
    <groupId>de.flapdoodle.embed</groupId>
    <artifactId>de.flapdoodle.embed.mongo.spring30x</artifactId>
    <version>4.11.0</version>
    <scope>test</scope>  <!-- Changed from runtime to test -->
</dependency>
```

---

## 🛠️ Troubleshooting

### Issue: Slow First Startup

**Symptom**: Service takes 2-5 minutes to start on first run

**Cause**: Flapdoodle is downloading MongoDB binaries (~100MB)

**Solution**: 
- ✅ Be patient on first run
- ✅ Subsequent runs will be fast (binaries are cached)
- ✅ Check internet connection if download is very slow

### Issue: Port Already in Use

**Symptom**: 
```
ERROR --- [main] de.flapdoodle.embed.mongo.MongodExecutable : Could not start mongod
Caused by: java.net.BindException: Address already in use
```

**Cause**: Another MongoDB instance is running on the same port

**Solution**:
```bash
# Find and kill MongoDB process
# Windows
netstat -ano | findstr :27017
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :27017
kill -9 <PID>

# Or restart the service
```

### Issue: MongoDB Binary Download Failed

**Symptom**:
```
ERROR --- [main] de.flapdoodle.embed.process.store.Downloader : Download failed
```

**Cause**: Network issues or firewall blocking download

**Solution**:
1. Check internet connection
2. Check firewall settings
3. Try downloading manually from https://fastdl.mongodb.org/
4. Or use real MongoDB for dev profile

### Issue: Data Not Persisting

**Symptom**: Data disappears after service restart

**Cause**: This is **expected behavior** with embedded MongoDB

**Solution**:
- ✅ Use `dev` profile with real MongoDB for persistent data
- ✅ Or accept temporary data for local development

### Issue: Redis Connection Failed

**Symptom**:
```
ERROR --- [main] o.s.data.redis.core.RedisConnectionUtils : Cannot get Jedis connection
```

**Cause**: Redis is not running locally

**Solution**:
```bash
# Install and start Redis
# Windows (using Chocolatey)
choco install redis-64
redis-server

# Linux
sudo apt-get install redis-server
sudo systemctl start redis

# macOS
brew install redis
brew services start redis

# Or use embedded-redis dependency (see above)
```

---

## ✅ Verification Checklist

### Local Environment (Embedded MongoDB)
- [ ] Service starts without MongoDB installation
- [ ] Flapdoodle downloads binaries on first run
- [ ] Embedded MongoDB starts automatically
- [ ] Collections are created automatically
- [ ] Products can be created and retrieved
- [ ] Data is cleared on service restart
- [ ] Debug logging visible in console

### Dev Environment (Real MongoDB)
- [ ] MongoDB connection successful
- [ ] Collections created with auto-indexing
- [ ] Data persists between restarts
- [ ] MongoDB logs visible
- [ ] Products can be created and retrieved

---

## 📚 Summary

| Aspect | Details |
|--------|---------|
| **Embedded MongoDB** | Flapdoodle 4.11.0 |
| **Profiles Using Embedded** | local, review |
| **Profiles Using Real MongoDB** | dev, sit, qa, pp, prod |
| **Installation Required** | ❌ No (for local/review) |
| **Data Persistence** | ❌ Lost on restart (local/review) |
| **First Run Download** | ~100MB (cached for subsequent runs) |
| **Startup Time** | 2-5 min (first run), 5-10 sec (subsequent) |
| **Redis** | Still requires local Redis or embedded-redis |
| **Production Ready** | ❌ No (use real MongoDB) |

---

## 🎉 Benefits

✅ **No MongoDB Installation** - Start catalog-service immediately  
✅ **Consistent Development Experience** - Same as H2 for auth/order services  
✅ **Fast Iteration** - No database setup or cleanup  
✅ **Isolated Testing** - Each service instance gets its own database  
✅ **Easy Switching** - Simple profile activation to use real MongoDB  
✅ **Production Path** - Clear migration to real MongoDB when ready  

---

## 🚀 Next Steps

1. ✅ **Start catalog-service** - Works immediately with embedded MongoDB
   ```bash
   cd catalog-service
   mvn spring-boot:run
   ```

2. ✅ **Test product creation** - Verify embedded MongoDB works

3. ✅ **Restart service** - Confirm data is cleared (expected)

4. ⬜ **Install real MongoDB** - When ready for persistent storage

5. ⬜ **Switch to dev profile** - For development with persistence

6. ⬜ **Configure production MongoDB** - Before production deployment

---

**Status**: ✅ **Embedded MongoDB configured for catalog-service**  
**Profiles**: local, review (embedded), dev/sit/qa/pp/prod (real MongoDB)  
**Installation Required**: ❌ No (for local/review profiles)  
**Data Persistence**: ❌ Lost on restart (local/review)  
**Ready to Use**: Catalog service can start immediately without MongoDB installation! 🚀
