# 🔧 Embedded MongoDB Troubleshooting Guide - Catalog Service Startup Error

## ❌ Error: Connection refused to localhost:27017

### Problem Description

When starting the catalog-service with the `local` profile, you see this error:

```
INFO 43456 --- [catalog-service] [localhost:27017] org.mongodb.driver.cluster : Exception in monitor thread while connecting to server localhost:27017
com.mongodb.MongoSocketOpenException: Exception opening socket
Caused by: java.net.ConnectException: Connection refused: getsockopt
```

**This means**: The catalog service is trying to connect to a **real MongoDB instance** at `localhost:27017` instead of using **embedded MongoDB (Flapdoodle)**.

---

## 🔍 Root Cause

The issue occurs because:

1. **`auto-index-creation: true` in base config** triggers Spring Data MongoDB to connect immediately on startup
2. **Before embedded MongoDB starts**, Spring tries to connect to the default MongoDB port (27017)
3. **No MongoDB running** on port 27017, so connection is refused
4. **Embedded MongoDB never gets a chance to start** because Spring fails early

---

## ✅ Solution: Remove auto-index-creation from Base Config

### Step 1: Update Base Configuration

**File**: `config-repo/catalog-service/application.yml`

**BEFORE** (Causes the error):
```yaml
spring:
  application:
    name: catalog-service
  data:
    mongodb:
      database: catalog_db
      auto-index-creation: true  # ❌ This triggers immediate connection
```

**AFTER** (Fixed):
```yaml
spring:
  application:
    name: catalog-service
  data:
    mongodb:
      database: catalog_db
      # auto-index-creation moved to environment-specific configs
      # to avoid triggering MongoDB connection in local/review profiles
```

### Step 2: Add auto-index-creation to Environment-Specific Configs

**For environments using real MongoDB** (dev, sit, qa, pp, prod), add `auto-index-creation` to each environment file:

**Example**: `config-repo/catalog-service/application-dev.yml`

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/catalog_db
      auto-index-creation: true  # ✅ Safe here because real MongoDB is available
```

**For local/review profiles**, do NOT add `auto-index-creation` (embedded MongoDB handles this automatically).

---

## 🚀 Complete Fix Implementation

### Files to Modify

1. **config-repo/catalog-service/application.yml** - Remove `auto-index-creation`
2. **config-repo/catalog-service/application-sit.yml** - Add `auto-index-creation: true`
3. **config-repo/catalog-service/application-qa.yml** - Add `auto-index-creation: true`
4. **config-repo/catalog-service/application-pp.yml** - Add `auto-index-creation: false` (production-like)
5. **config-repo/catalog-service/application-prod.yml** - Add `auto-index-creation: false` (production)

### Updated Configuration Files

#### Base Configuration (application.yml)

```yaml
# Catalog Service Configuration

server:
  port: 8082

spring:
  application:
    name: catalog-service
  data:
    mongodb:
      database: catalog_db
      # auto-index-creation moved to environment-specific configs
      # to avoid triggering MongoDB connection in local/review profiles
  redis:
    timeout: 60000
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

cache:
  ttl:
    products: 600  # 10 minutes
    categories: 1800  # 30 minutes

logging:
  level:
    com.ecommerce.catalog: DEBUG
    org.springframework.data.mongodb: DEBUG
```

#### Local Profile (application-local.yml) - No Changes Needed

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
      # ✅ No auto-index-creation here - embedded MongoDB handles this

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

#### Dev Profile (application-dev.yml)

```yaml
# Catalog Service - Development Environment Configuration
# Uses local MongoDB and Redis

spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/catalog_db
      auto-index-creation: true  # ✅ Enable for dev
  
  redis:
    host: localhost
    port: 6379
  
  cache:
    type: redis

logging:
  level:
    root: DEBUG
    com.ecommerce.catalog: DEBUG
    org.springframework.data.mongodb: DEBUG
```

#### SIT/QA Profiles

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://${MONGO_USERNAME:ecommerce_user}:${MONGO_PASSWORD}@${MONGO_HOST:sit-mongo}:27017/catalog_db?authSource=admin
      auto-index-creation: true  # ✅ Enable for testing environments
```

#### PP/Prod Profiles

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://${MONGO_USERNAME}:${MONGO_PASSWORD}@${MONGO_HOST}:27017/catalog_db?authSource=admin
      auto-index-creation: false  # ✅ Disable for production (manual index management)
```

---

## 🧪 Verification Steps

### Step 1: Clean and Rebuild

```bash
cd catalog-service
mvn clean install
```

### Step 2: Start Catalog Service

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Step 3: Verify Successful Startup

**Expected Logs** (in order):

```
INFO --- [main] c.e.catalog.CatalogServiceApplication    : Starting CatalogServiceApplication
INFO --- [main] c.e.catalog.CatalogServiceApplication    : The following 1 profile is active: "local"
INFO --- [main] o.s.c.c.c.ConfigServerConfigDataLoader   : Fetching config from server at : http://localhost:8888
INFO --- [main] de.flapdoodle.embed.mongo.MongodExecutable : Starting mongod...
INFO --- [main] de.flapdoodle.embed.mongo.MongodProcess    : mongod started
INFO --- [main] org.mongodb.driver.cluster                 : Cluster created with settings {hosts=[localhost:xxxxx]}  ← Random port, NOT 27017
INFO --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer    : Tomcat started on port 8082 (http)
INFO --- [main] c.e.catalog.CatalogServiceApplication      : Started CatalogServiceApplication in 8.5 seconds
```

**Key Indicators of Success**:
- ✅ Profile is `local`
- ✅ `de.flapdoodle.embed.mongo` logs appear
- ✅ MongoDB connection is to a **random port** (not 27017)
- ✅ Service starts successfully on port 8082
- ❌ **NO** `Connection refused` errors

### Step 4: Test Product Creation

```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Test Product",
    "description": "Testing embedded MongoDB",
    "price": 99.99,
    "stock": 10,
    "category": "Test"
  }'
```

**Expected**: Product created successfully with a MongoDB ObjectId.

---

## 🔍 Troubleshooting

### Still Getting Connection Refused Error?

#### Check 1: Verify Active Profile

Look for this line in logs:
```
INFO --- [main] c.e.catalog.CatalogServiceApplication : The following 1 profile is active: "local"
```

If you see `"default"` instead, the profile is not set correctly.

**Fix**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

#### Check 2: Verify No MongoDB URI in Local Config

Ensure `config-repo/catalog-service/application-local.yml` does NOT have a `uri` property:

```yaml
spring:
  data:
    mongodb:
      database: catalog_db  # ✅ Only database name
      # ❌ NO uri property here
```

#### Check 3: Verify Flapdoodle Dependency

Check `catalog-service/pom.xml` contains:

```xml
<dependency>
    <groupId>de.flapdoodle.embed</groupId>
    <artifactId>de.flapdoodle.embed.mongo.spring30x</artifactId>
    <version>4.11.0</version>
    <scope>runtime</scope>
</dependency>
```

If missing, add it and run:
```bash
mvn clean install
```

#### Check 4: Verify Config Server is Running

Embedded MongoDB might not start if Config Server is not available and the service fails early.

```bash
# Test Config Server
curl http://admin:admin123@localhost:8888/actuator/health

# Test catalog-service local config
curl http://admin:admin123@localhost:8888/catalog-service/local
```

If Config Server is not running:
```bash
cd config-server
mvn spring-boot:run
```

---

## 📊 Configuration Priority Explanation

Spring Cloud Config loads configurations in this order (later overrides earlier):

```
1. config-repo/application.yml                    (Lowest priority)
   ↓
2. config-repo/application-local.yml              (Environment-level)
   ↓
3. config-repo/catalog-service/application.yml    (Service-level)
   ↓
4. config-repo/catalog-service/application-local.yml (Highest priority)
```

**Problem**: If `auto-index-creation: true` is in step 3 (service-level base), it applies to ALL profiles including local.

**Solution**: Remove it from step 3, add it only to environment-specific configs (step 4) for environments that need it.

---

## ✅ Summary of Changes

| File | Change | Reason |
|------|--------|--------|
| **config-repo/catalog-service/application.yml** | Removed `auto-index-creation` | Prevents immediate MongoDB connection attempt |
| **config-repo/catalog-service/application-dev.yml** | Already has `auto-index-creation: true` | Safe because real MongoDB is available |
| **config-repo/catalog-service/application-sit.yml** | Add `auto-index-creation: true` | Enable for testing |
| **config-repo/catalog-service/application-qa.yml** | Add `auto-index-creation: true` | Enable for testing |
| **config-repo/catalog-service/application-pp.yml** | Add `auto-index-creation: false` | Production-like (manual index management) |
| **config-repo/catalog-service/application-prod.yml** | Add `auto-index-creation: false` | Production (manual index management) |
| **config-repo/catalog-service/application-local.yml** | No change needed | Embedded MongoDB handles indexing automatically |
| **config-repo/catalog-service/application-review.yml** | No change needed | Embedded MongoDB handles indexing automatically |

---

## 🎯 Why This Fix Works

1. **No early connection attempt**: Without `auto-index-creation` in base config, Spring Data MongoDB doesn't try to connect immediately
2. **Embedded MongoDB starts first**: Flapdoodle has time to download binaries (first run) and start MongoDB
3. **Spring connects to embedded MongoDB**: After embedded MongoDB is ready, Spring connects to it
4. **Environment-specific control**: Each environment can enable/disable auto-index-creation as needed

---

## 🚀 Next Steps After Fix

1. ✅ **Apply the configuration changes** (remove `auto-index-creation` from base config)
2. ✅ **Rebuild the project** (`mvn clean install`)
3. ✅ **Start catalog-service** (`mvn spring-boot:run -Dspring-boot.run.profiles=local`)
4. ✅ **Verify successful startup** (check logs for embedded MongoDB startup)
5. ✅ **Test product creation** (verify embedded MongoDB works)
6. ✅ **Restart service** (verify data is cleared - expected behavior)

---

**Status**: ✅ **Fix identified and documented**  
**Root Cause**: `auto-index-creation: true` in base config triggers early MongoDB connection  
**Solution**: Move `auto-index-creation` to environment-specific configs only  
**Impact**: Embedded MongoDB can now start properly for local/review profiles  
**Ready to Apply**: Follow the steps above to fix the issue! 🛠️
