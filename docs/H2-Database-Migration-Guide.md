# H2 Database Migration Guide

## Overview

This guide documents the migration from PostgreSQL to H2 in-memory database for the E-Commerce Platform microservices. PostgreSQL configurations have been commented out for future use when you're ready to switch back to a persistent database.

---

## What Changed

### Services Migrated to H2

1. **Auth Service** (Port 8081)
2. **Order Service** (Port 8083)

Both services now use H2 in-memory database instead of PostgreSQL.

### Files Modified

#### Auth Service

**1. `auth-service/pom.xml`**
- ✅ Added H2 database dependency
- ✅ Commented out PostgreSQL dependency for future use

```xml
<!-- H2 Database (In-Memory) - PostgreSQL can be used later -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- PostgreSQL Driver - COMMENTED FOR FUTURE USE -->
<!--
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
-->
```

**2. `auth-service/src/main/resources/application.yml`**
- ✅ Changed datasource URL to H2 in-memory: `jdbc:h2:mem:auth_db`
- ✅ Updated username to `sa` (H2 default)
- ✅ Empty password (H2 default)
- ✅ Changed driver class to `org.h2.Driver`
- ✅ Enabled H2 console at `/h2-console`
- ✅ Changed Hibernate dialect to `H2Dialect`
- ✅ Changed `ddl-auto` to `create-drop` (recreates schema on each restart)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:auth_db
    username: sa
    password:
    driver-class-name: org.h2.Driver
  
  h2:
    console:
      enabled: true
      path: /h2-console
    
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
        format_sql: true
```

**3. `config-repo/auth-service/application.yml`**
- ✅ Same H2 configuration as above
- ✅ PostgreSQL configuration commented out for future use

---

#### Order Service

**1. `order-service/pom.xml`**
- ✅ Added H2 database dependency
- ✅ Commented out PostgreSQL dependency for future use

**2. `order-service/src/main/resources/application.yml`**
- ✅ Changed datasource URL to H2 in-memory: `jdbc:h2:mem:order_db`
- ✅ Updated username to `sa`
- ✅ Empty password
- ✅ Changed driver class to `org.h2.Driver`
- ✅ Enabled H2 console at `/h2-console`
- ✅ Changed Hibernate dialect to `H2Dialect`
- ✅ Changed `ddl-auto` to `create-drop`

**3. `config-repo/order-service/application.yml`**
- ✅ Same H2 configuration as above
- ✅ PostgreSQL configuration commented out for future use

---

#### Shared Configuration Files

**1. `config-repo/application-dev.yml`**
- ✅ Changed default datasource to H2: `jdbc:h2:mem:ecommerce_db`
- ✅ Enabled H2 console
- ✅ PostgreSQL configuration commented out

**2. `config-repo/application-prod.yml`**
- ✅ Changed datasource to H2 (for now)
- ✅ H2 console disabled in production for security
- ✅ PostgreSQL configuration commented out

---

## H2 Database Features

### Advantages

✅ **No Installation Required** - H2 runs in-memory, no separate database server needed  
✅ **Fast Startup** - No connection overhead, instant availability  
✅ **Zero Configuration** - Works out of the box  
✅ **Perfect for Development** - Quick iteration and testing  
✅ **Built-in Console** - Web-based database viewer at `/h2-console`  
✅ **No Connection Errors** - Eliminates "Connection refused" errors  

### Limitations

❌ **Data is Lost on Restart** - In-memory database, all data cleared when service stops  
❌ **Not for Production** - Should migrate to PostgreSQL for production deployment  
❌ **No Data Persistence** - Cannot store data permanently  
❌ **Limited for Testing** - Cannot test database-specific features  

---

## How to Use H2 Database

### Starting Services

No database setup required! Just start your services:

```powershell
# Start Config Server
cd config-server
mvn spring-boot:run

# Start Auth Service
cd auth-service
mvn spring-boot:run

# Start Order Service
cd order-service
mvn spring-boot:run
```

### Accessing H2 Console

Each service has its own H2 console:

**Auth Service H2 Console:**
- URL: http://localhost:8081/h2-console
- JDBC URL: `jdbc:h2:mem:auth_db`
- Username: `sa`
- Password: (leave empty)

**Order Service H2 Console:**
- URL: http://localhost:8083/h2-console
- JDBC URL: `jdbc:h2:mem:order_db`
- Username: `sa`
- Password: (leave empty)

### Viewing Database Tables

1. Open H2 console in browser (e.g., http://localhost:8081/h2-console)
2. Enter connection details:
   - **JDBC URL**: `jdbc:h2:mem:auth_db`
   - **Username**: `sa`
   - **Password**: (empty)
3. Click "Connect"
4. You'll see all tables created by Hibernate
5. Run SQL queries to view data:

```sql
-- View all users
SELECT * FROM users;

-- View all orders
SELECT * FROM orders;
```

---

## Database Schema Management

### Current Configuration: `create-drop`

```yaml
jpa:
  hibernate:
    ddl-auto: create-drop
```

**Behavior:**
- ✅ Creates all tables on service startup
- ✅ Drops all tables on service shutdown
- ✅ Fresh database on every restart
- ✅ Perfect for development and testing

### Alternative Options

If you want to keep data during development sessions (until service restart):

```yaml
jpa:
  hibernate:
    ddl-auto: update  # Updates schema but keeps data during runtime
```

**Note:** Data is still lost on service restart because H2 is in-memory.

---

## Testing with H2

### Auth Service Testing

**1. Register a User**
```powershell
curl -X POST http://localhost:8081/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{"email":"test@example.com","password":"password123","firstName":"John","lastName":"Doe"}'
```

**2. Login**
```powershell
curl -X POST http://localhost:8081/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"test@example.com","password":"password123"}'
```

**3. View User in H2 Console**
- Open http://localhost:8081/h2-console
- Connect with `jdbc:h2:mem:auth_db`
- Run: `SELECT * FROM users;`

### Order Service Testing

**1. Create an Order**
```powershell
curl -X POST http://localhost:8083/api/orders `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_JWT_TOKEN" `
  -d '{"items":[{"productId":"123","quantity":2}]}'
```

**2. View Order in H2 Console**
- Open http://localhost:8083/h2-console
- Connect with `jdbc:h2:mem:order_db`
- Run: `SELECT * FROM orders;`

---

## Migrating Back to PostgreSQL

When you're ready to use PostgreSQL for production or persistent storage:

### Step 1: Uncomment PostgreSQL Dependencies

**In `auth-service/pom.xml` and `order-service/pom.xml`:**

```xml
<!-- Uncomment this -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Comment out or remove H2 -->
<!--
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
-->
```

### Step 2: Update Configuration Files

**In `auth-service/src/main/resources/application.yml`:**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_db
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
    
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
```

**Repeat for:**
- `order-service/src/main/resources/application.yml`
- `config-repo/auth-service/application.yml`
- `config-repo/order-service/application.yml`
- `config-repo/application-dev.yml`
- `config-repo/application-prod.yml`

### Step 3: Install and Start PostgreSQL

```powershell
# Start PostgreSQL service
net start postgresql-x64-14

# Create databases
psql -U postgres -h localhost -c "CREATE DATABASE auth_db;"
psql -U postgres -h localhost -c "CREATE DATABASE order_db;"
```

### Step 4: Rebuild and Restart Services

```powershell
# Rebuild with Maven
mvn clean install

# Start services
cd auth-service
mvn spring-boot:run

cd order-service
mvn spring-boot:run
```

---

## Configuration Comparison

| Feature | H2 In-Memory | PostgreSQL |
|---------|-------------|------------|
| **Installation** | None required | Requires PostgreSQL server |
| **Startup Time** | Instant | Requires DB connection |
| **Data Persistence** | ❌ Lost on restart | ✅ Persistent |
| **Web Console** | ✅ Built-in | ❌ Requires pgAdmin |
| **Production Ready** | ❌ No | ✅ Yes |
| **Development** | ✅ Perfect | ⚠️ Requires setup |
| **Testing** | ✅ Fast | ⚠️ Slower |
| **Connection Errors** | ❌ Never | ⚠️ Possible |

---

## Troubleshooting

### Issue: H2 Console Not Accessible

**Solution:**
Ensure H2 console is enabled in `application.yml`:

```yaml
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
```

### Issue: Tables Not Created

**Solution:**
Check Hibernate DDL setting:

```yaml
jpa:
  hibernate:
    ddl-auto: create-drop  # or 'update'
```

### Issue: Data Lost After Restart

**Explanation:**
This is expected behavior with H2 in-memory database. Data is stored in RAM and cleared on shutdown.

**Solution:**
If you need persistence:
1. Use file-based H2: `jdbc:h2:file:./data/auth_db`
2. Or migrate to PostgreSQL (see migration guide above)

### Issue: Cannot Connect to H2 Console

**Checklist:**
- [ ] Service is running
- [ ] Using correct port (8081 for auth-service, 8083 for order-service)
- [ ] JDBC URL matches: `jdbc:h2:mem:auth_db` or `jdbc:h2:mem:order_db`
- [ ] Username is `sa`
- [ ] Password is empty

---

## Summary

### What You Can Do Now

✅ **Start services without PostgreSQL** - No database installation required  
✅ **Develop and test locally** - Fast iteration with in-memory database  
✅ **View data in H2 console** - Web-based database viewer  
✅ **No connection errors** - Eliminates PostgreSQL connection issues  
✅ **Switch to PostgreSQL later** - All configurations preserved and commented  

### What to Remember

⚠️ **Data is temporary** - Lost on service restart  
⚠️ **Not for production** - Migrate to PostgreSQL before deployment  
⚠️ **Each service has separate database** - `auth_db` and `order_db` are independent  

---

## Next Steps

1. ✅ **Start all services** - No PostgreSQL required
2. ✅ **Test functionality** - Register users, create orders
3. ✅ **View data in H2 console** - Inspect tables and data
4. ✅ **Develop features** - Fast iteration without database overhead
5. ⬜ **Migrate to PostgreSQL** - When ready for production or persistent storage

---

## Files Modified Summary

### Auth Service
- `auth-service/pom.xml` - H2 dependency added, PostgreSQL commented
- `auth-service/src/main/resources/application.yml` - H2 configuration
- `config-repo/auth-service/application.yml` - H2 configuration

### Order Service
- `order-service/pom.xml` - H2 dependency added, PostgreSQL commented
- `order-service/src/main/resources/application.yml` - H2 configuration
- `config-repo/order-service/application.yml` - H2 configuration

### Shared Configuration
- `config-repo/application-dev.yml` - H2 default datasource
- `config-repo/application-prod.yml` - H2 configuration (temporary)

---

**Migration Date**: August 6, 2026  
**Reason**: Eliminate PostgreSQL connection errors during development  
**Future Plan**: Migrate to PostgreSQL for production deployment  
**Status**: ✅ Complete - All services using H2 in-memory database
