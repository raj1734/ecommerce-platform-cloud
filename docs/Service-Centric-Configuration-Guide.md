# Service-Centric Configuration Hierarchy Guide

## Overview

This guide documents the **service-centric configuration hierarchy** implemented for the E-Commerce Platform. Each microservice has its own set of environment-specific configuration files, providing maximum flexibility and service ownership.

---

## Configuration Structure

### Complete File Hierarchy

```
config-repo/
├── application.yml                           # Common base configuration
├── application-local.yml                     # Local environment (root level)
├── application-dev.yml                       # Development environment (root level)
├── application-sit.yml                       # SIT environment (root level)
├── application-qa.yml                        # QA environment (root level)
├── application-review.yml                    # Review environment (root level)
├── application-pp.yml                        # Pre-production environment (root level)
├── application-prod.yml                      # Production environment (root level)
│
├── auth-service/
│   ├── application.yml                       # Auth service base config
│   ├── application-local.yml                 # Auth service - local env
│   ├── application-dev.yml                   # Auth service - dev env
│   ├── application-sit.yml                   # Auth service - SIT env
│   ├── application-qa.yml                    # Auth service - QA env
│   ├── application-review.yml                # Auth service - review env
│   ├── application-pp.yml                    # Auth service - PP env
│   └── application-prod.yml                  # Auth service - prod env
│
├── catalog-service/
│   ├── application.yml                       # Catalog service base config
│   ├── application-local.yml                 # Catalog service - local env
│   ├── application-dev.yml                   # Catalog service - dev env
│   ├── application-sit.yml                   # Catalog service - SIT env
│   ├── application-qa.yml                    # Catalog service - QA env
│   ├── application-review.yml                # Catalog service - review env
│   ├── application-pp.yml                    # Catalog service - PP env
│   └── application-prod.yml                  # Catalog service - prod env
│
├── order-service/
│   ├── application.yml                       # Order service base config
│   ├── application-local.yml                 # Order service - local env
│   ├── application-dev.yml                   # Order service - dev env
│   ├── application-sit.yml                   # Order service - SIT env
│   ├── application-qa.yml                    # Order service - QA env
│   ├── application-review.yml                # Order service - review env
│   ├── application-pp.yml                    # Order service - PP env
│   └── application-prod.yml                  # Order service - prod env
│
└── gateway-service/
    ├── application.yml                       # Gateway service base config
    ├── application-local.yml                 # Gateway service - local env
    ├── application-dev.yml                   # Gateway service - dev env
    ├── application-sit.yml                   # Gateway service - SIT env
    ├── application-qa.yml                    # Gateway service - QA env
    ├── application-review.yml                # Gateway service - review env
    ├── application-pp.yml                    # Gateway service - PP env
    └── application-prod.yml                  # Gateway service - prod env

Total Files: 40 configuration files
- Root level: 8 files (application.yml + 7 environment files)
- Per service: 8 files each × 4 services = 32 files
```

---

## Configuration Loading Priority

Spring Cloud Config loads and merges configurations in this order (later overrides earlier):

```
1. config-repo/application.yml                    (Lowest priority - common base)
   ↓
2. config-repo/application-{profile}.yml          (Root environment config)
   ↓
3. config-repo/{service}/application.yml          (Service-specific base)
   ↓
4. config-repo/{service}/application-{profile}.yml (Highest priority - service + environment)
```

### Example: Auth Service with `sit` Profile

**Loading Order**:
```
1. config-repo/application.yml
2. config-repo/application-sit.yml
3. config-repo/auth-service/application.yml
4. config-repo/auth-service/application-sit.yml  ← Highest priority
```

**Result**: Service-specific environment configuration (`auth-service/application-sit.yml`) overrides all other settings.

---

## Environment Profiles

### 1. Local Profile (`local`)

**Purpose**: Quick local development without external dependencies

**Key Features**:
- ✅ H2 in-memory database (auth-service, order-service)
- ✅ Local MongoDB (catalog-service)
- ✅ Local Redis (catalog-service, gateway-service)
- ✅ Local Kafka (order-service)
- ✅ H2 console enabled at `/h2-console`
- ✅ Debug logging (TRACE/DEBUG levels)
- ✅ SQL logging enabled
- ❌ Data lost on restart (expected for H2)

**Startup**:
```bash
# Default profile (no need to specify)
cd auth-service
mvn spring-boot:run

# Or explicitly
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**H2 Console Access**:
- **Auth Service**: http://localhost:8081/h2-console
  - JDBC URL: `jdbc:h2:mem:auth_db`
  - Username: `sa`
  - Password: (empty)

- **Order Service**: http://localhost:8083/h2-console
  - JDBC URL: `jdbc:h2:mem:order_db`
  - Username: `sa`
  - Password: (empty)

---

### 2. Development Profile (`dev`)

**Purpose**: Development with persistent storage

**Key Features**:
- ✅ PostgreSQL (auth-service, order-service)
- ✅ MongoDB (catalog-service)
- ✅ Redis (catalog-service, gateway-service)
- ✅ Kafka (order-service)
- ✅ Localhost connections with default credentials
- ✅ Debug logging
- ✅ SQL logging enabled
- ✅ Hibernate DDL: `update` (auto-create/update tables)

**Startup**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Prerequisites**:
```bash
# Start PostgreSQL
net start postgresql-x64-14

# Create databases
psql -U postgres -c "CREATE DATABASE auth_db;"
psql -U postgres -c "CREATE DATABASE order_db;"

# Start MongoDB
net start MongoDB

# Start Redis
redis-server

# Start Kafka
bin/kafka-server-start.sh config/server.properties
```

---

### 3. SIT Profile (`sit`)

**Purpose**: System Integration Testing

**Key Features**:
- ✅ Remote PostgreSQL (SIT database servers)
- ✅ Remote MongoDB (SIT MongoDB cluster)
- ✅ Remote Redis (SIT Redis instance)
- ✅ Remote Kafka (SIT Kafka brokers)
- ✅ Environment variables for credentials
- ✅ Connection pooling (max: 10, min: 5)
- ✅ INFO level logging (DEBUG for application)
- ✅ Hibernate DDL: `update`

**Startup**:
```bash
# Set environment variables
export DB_HOST=sit-db.example.com
export DB_USERNAME=ecommerce_user
export DB_PASSWORD=sit_password
export MONGO_HOST=sit-mongo.example.com
export MONGO_USERNAME=ecommerce_user
export MONGO_PASSWORD=sit_mongo_password
export REDIS_HOST=sit-redis.example.com
export REDIS_PASSWORD=sit_redis_password
export KAFKA_BROKERS=sit-kafka.example.com:9092

# Start service
mvn spring-boot:run -Dspring-boot.run.profiles=sit
```

---

### 4. QA Profile (`qa`)

**Purpose**: Quality Assurance testing

**Key Features**:
- ✅ Remote PostgreSQL (QA database servers)
- ✅ Remote MongoDB (QA MongoDB cluster)
- ✅ Remote Redis (QA Redis instance)
- ✅ Remote Kafka (QA Kafka brokers)
- ✅ Environment variables for credentials
- ✅ Connection pooling (max: 10, min: 5)
- ✅ INFO level logging (DEBUG for application)
- ✅ Hibernate DDL: `update`

**Startup**:
```bash
# Set environment variables
export DB_HOST=qa-db.example.com
export DB_USERNAME=ecommerce_user
export DB_PASSWORD=qa_password
# ... (similar to SIT)

# Start service
mvn spring-boot:run -Dspring-boot.run.profiles=qa
```

---

### 5. Review Profile (`review`)

**Purpose**: Code review and PR testing

**Key Features**:
- ✅ H2 in-memory database (auth-service, order-service)
- ✅ Local MongoDB (catalog-service)
- ✅ Local Redis (catalog-service, gateway-service)
- ✅ Local Kafka (order-service)
- ✅ H2 console enabled
- ✅ INFO level logging (DEBUG for application)
- ✅ SQL logging enabled
- ❌ Data lost on restart

**Startup**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=review
```

**Use Case**: Perfect for CI/CD pipelines and PR validation without external dependencies.

---

### 6. Pre-Production Profile (`pp`)

**Purpose**: Production-like environment for final testing

**Key Features**:
- ✅ Remote PostgreSQL (PP database servers)
- ✅ Remote MongoDB (PP MongoDB cluster)
- ✅ Remote Redis (PP Redis instance)
- ✅ Remote Kafka (PP Kafka brokers)
- ✅ **Mandatory environment variables** (no defaults)
- ✅ Larger connection pool (max: 15, min: 10)
- ✅ **Hibernate DDL: `validate`** (no schema changes allowed)
- ✅ INFO level logging
- ✅ MongoDB auto-index creation disabled

**Startup**:
```bash
# Set environment variables (all mandatory)
export DB_HOST=pp-db.example.com
export DB_USERNAME=ecommerce_user
export DB_PASSWORD=pp_password
export MONGO_HOST=pp-mongo.example.com
export MONGO_USERNAME=ecommerce_user
export MONGO_PASSWORD=pp_mongo_password
export REDIS_HOST=pp-redis.example.com
export REDIS_PASSWORD=pp_redis_password
export KAFKA_BROKERS=pp-kafka.example.com:9092

# Start service
mvn spring-boot:run -Dspring-boot.run.profiles=pp
```

**Important**: Schema validation only - database schema must match entity definitions.

---

### 7. Production Profile (`prod`)

**Purpose**: Live production environment

**Key Features**:
- ✅ Remote PostgreSQL (Production database servers)
- ✅ Remote MongoDB (Production MongoDB cluster)
- ✅ Remote Redis (Production Redis cluster with SSL)
- ✅ Remote Kafka (Production Kafka cluster)
- ✅ **All environment variables mandatory** (no defaults)
- ✅ Maximum connection pool (max: 20, min: 10)
- ✅ **Hibernate DDL: `validate`** (no schema changes)
- ✅ INFO level logging only
- ✅ MongoDB auto-index creation disabled
- ✅ Redis SSL enabled
- ✅ Optimized connection timeouts

**Startup**:
```bash
# Set environment variables (all mandatory, no defaults)
export DB_HOST=prod-db.example.com
export DB_USERNAME=prod_user
export DB_PASSWORD=prod_secure_password
export MONGO_HOST=prod-mongo.example.com
export MONGO_USERNAME=prod_user
export MONGO_PASSWORD=prod_mongo_password
export REDIS_HOST=prod-redis.example.com
export REDIS_PASSWORD=prod_redis_password
export KAFKA_BROKERS=prod-kafka1:9092,prod-kafka2:9092,prod-kafka3:9092
export JWT_SECRET=production-256-bit-secret-key

# Start service
java -jar auth-service.jar --spring.profiles.active=prod
```

---

## Service-Specific Configuration Details

### Auth Service

**Database Configuration by Environment**:

| Environment | Database | JDBC URL | DDL Auto | H2 Console |
|-------------|----------|----------|----------|------------|
| **local** | H2 | `jdbc:h2:mem:auth_db` | `create-drop` | ✅ Enabled |
| **dev** | PostgreSQL | `jdbc:postgresql://localhost:5432/auth_db` | `update` | ❌ Disabled |
| **sit** | PostgreSQL | `jdbc:postgresql://sit-db.example.com:5432/auth_db` | `update` | ❌ Disabled |
| **qa** | PostgreSQL | `jdbc:postgresql://qa-db.example.com:5432/auth_db` | `update` | ❌ Disabled |
| **review** | H2 | `jdbc:h2:mem:auth_db` | `create-drop` | ✅ Enabled |
| **pp** | PostgreSQL | `jdbc:postgresql://pp-db.example.com:5432/auth_db` | `validate` | ❌ Disabled |
| **prod** | PostgreSQL | `jdbc:postgresql://${DB_HOST}:5432/auth_db` | `validate` | ❌ Disabled |

**JWT Configuration**:
- Inherited from `auth-service/application.yml`
- Secret: `${JWT_SECRET:your-256-bit-secret-key-change-in-production}`
- Access token expiration: 24 hours (86400000 ms)
- Refresh token expiration: 7 days (604800000 ms)

---

### Order Service

**Database Configuration by Environment**:

| Environment | Database | JDBC URL | DDL Auto | Kafka Brokers |
|-------------|----------|----------|----------|---------------|
| **local** | H2 | `jdbc:h2:mem:order_db` | `create-drop` | `localhost:9092` |
| **dev** | PostgreSQL | `jdbc:postgresql://localhost:5432/order_db` | `update` | `localhost:9092` |
| **sit** | PostgreSQL | `jdbc:postgresql://sit-db.example.com:5432/order_db` | `update` | `sit-kafka.example.com:9092` |
| **qa** | PostgreSQL | `jdbc:postgresql://qa-db.example.com:5432/order_db` | `update` | `qa-kafka.example.com:9092` |
| **review** | H2 | `jdbc:h2:mem:order_db` | `create-drop` | `localhost:9092` |
| **pp** | PostgreSQL | `jdbc:postgresql://pp-db.example.com:5432/order_db` | `validate` | `${KAFKA_BROKERS}` |
| **prod** | PostgreSQL | `jdbc:postgresql://${DB_HOST}:5432/order_db` | `validate` | `${KAFKA_BROKERS}` |

**Kafka Configuration**:
- Inherited from `order-service/application.yml`
- Producer: JSON serialization, acks=all, retries=3
- Consumer: Group ID `order-service-group`, JSON deserialization
- Topics: `order-created-topic`, `order-updated-topic`

**Resilience4j Configuration**:
- Circuit Breaker: 50% failure threshold over 10 calls
- Retry: 3 attempts with exponential backoff (1s, 2s, 4s)

---

### Catalog Service

**Database Configuration by Environment**:

| Environment | MongoDB URI | Redis Host | Auto-Index |
|-------------|------------|------------|------------|
| **local** | `mongodb://localhost:27017/catalog_db` | `localhost` | N/A |
| **dev** | `mongodb://localhost:27017/catalog_db` | `localhost` | ✅ Enabled |
| **sit** | `mongodb://user:pass@sit-mongo.example.com:27017/catalog_db` | `sit-redis.example.com` | ✅ Enabled |
| **qa** | `mongodb://user:pass@qa-mongo.example.com:27017/catalog_db` | `qa-redis.example.com` | ✅ Enabled |
| **review** | `mongodb://localhost:27017/catalog_db` | `localhost` | N/A |
| **pp** | `mongodb://user:pass@pp-mongo.example.com:27017/catalog_db` | `pp-redis.example.com` | ❌ Disabled |
| **prod** | `mongodb://user:pass@${MONGO_HOST}:27017/catalog_db` | `${REDIS_HOST}` | ❌ Disabled |

**Cache Configuration**:
- Inherited from `catalog-service/application.yml`
- Product TTL: 10 minutes (600 seconds)
- Category TTL: 30 minutes (1800 seconds)
- Cache type: Redis

---

### Gateway Service

**Redis Configuration by Environment**:

| Environment | Redis Host | Redis Password | SSL |
|-------------|-----------|----------------|-----|
| **local** | `localhost` | N/A | ❌ |
| **dev** | `localhost` | N/A | ❌ |
| **sit** | `sit-redis.example.com` | `${REDIS_PASSWORD}` | ❌ |
| **qa** | `qa-redis.example.com` | `${REDIS_PASSWORD}` | ❌ |
| **review** | `localhost` | N/A | ❌ |
| **pp** | `pp-redis.example.com` | `${REDIS_PASSWORD}` | ❌ |
| **prod** | `${REDIS_HOST}` | `${REDIS_PASSWORD}` | ✅ |

**Routes Configuration**:
- Inherited from `gateway-service/application.yml`
- `/api/auth/**` → auth-service
- `/api/catalog/**` → catalog-service
- `/api/orders/**` → order-service
- StripPrefix filter applied to all routes

**Rate Limiting**:
- Replenish rate: 10 requests/second
- Burst capacity: 20 requests
- Uses Redis for distributed rate limiting

---

## Testing Configuration

### Verify Config Server Endpoints

```bash
# Test local profile for auth-service
curl http://admin:admin123@localhost:8888/auth-service/local

# Test dev profile for catalog-service
curl http://admin:admin123@localhost:8888/catalog-service/dev

# Test sit profile for order-service
curl http://admin:admin123@localhost:8888/order-service/sit

# Test prod profile for gateway-service
curl http://admin:admin123@localhost:8888/gateway-service/prod
```

**Expected Response Structure**:
```json
{
  "name": "auth-service",
  "profiles": ["local"],
  "propertySources": [
    {
      "name": "file:./config-repo/auth-service/application-local.yml",
      "source": {
        "spring.datasource.url": "jdbc:h2:mem:auth_db",
        "spring.h2.console.enabled": true
      }
    },
    {
      "name": "file:./config-repo/application-local.yml",
      "source": { ... }
    },
    {
      "name": "file:./config-repo/auth-service/application.yml",
      "source": {
        "server.port": 8081,
        "jwt.secret": "..."
      }
    },
    {
      "name": "file:./config-repo/application.yml",
      "source": { ... }
    }
  ]
}
```

---

## Environment Variables Reference

### Required for PostgreSQL Environments (dev, sit, qa, pp, prod)

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | `sit-db.example.com` |
| `DB_USERNAME` | Database username | `ecommerce_user` |
| `DB_PASSWORD` | Database password | `secure_password` |

### Required for MongoDB (Catalog Service)

| Variable | Description | Example |
|----------|-------------|---------|
| `MONGO_HOST` | MongoDB host | `sit-mongo.example.com` |
| `MONGO_USERNAME` | MongoDB username | `ecommerce_user` |
| `MONGO_PASSWORD` | MongoDB password | `mongo_password` |

### Required for Redis (Catalog & Gateway Services)

| Variable | Description | Example |
|----------|-------------|---------|
| `REDIS_HOST` | Redis host | `sit-redis.example.com` |
| `REDIS_PASSWORD` | Redis password | `redis_password` |

### Required for Kafka (Order Service)

| Variable | Description | Example |
|----------|-------------|---------|
| `KAFKA_BROKERS` | Kafka broker list | `kafka1:9092,kafka2:9092` |

### Required for JWT (Auth & Gateway Services)

| Variable | Description | Example |
|----------|-------------|---------|
| `JWT_SECRET` | JWT signing key (256-bit) | `your-256-bit-secret-key` |

---

## Advantages of Service-Centric Hierarchy

### ✅ Pros

1. **Service Ownership**: Each service team owns all their configurations
2. **Service-Specific Overrides**: Easy to customize any environment for a specific service
3. **Clear Service Boundaries**: All configs for a service in one folder
4. **Independent Service Evolution**: Services can evolve configurations independently
5. **Service-Oriented Teams**: Aligns with microservices team structure
6. **Granular Control**: Maximum flexibility per service per environment

### ❌ Cons

1. **File Duplication**: 40 total files (potential redundancy)
2. **Environment-Wide Changes**: Must edit multiple files (4 services × 1 environment)
3. **Maintenance Overhead**: More files to keep in sync
4. **Potential Inconsistency**: Easy to forget updating a service in one environment
5. **Complex for New Developers**: Requires understanding of file hierarchy

---

## Best Practices

### 1. Keep Root-Level Configs Minimal

Root-level environment files (`application-{profile}.yml`) should only contain:
- Common database connection patterns
- Shared logging levels
- Common infrastructure endpoints

**Avoid**: Service-specific settings in root files

### 2. Use Service-Level Configs for Specifics

Service-level environment files (`{service}/application-{profile}.yml`) should contain:
- Service-specific database URLs
- Service-specific connection pool settings
- Service-specific logging levels
- Service-specific feature flags

### 3. Document Environment Differences

Add comments in each environment file explaining:
- Why this environment differs from others
- What external dependencies are required
- Any special setup instructions

### 4. Version Control Best Practices

```bash
# Commit service-specific changes separately
git add config-repo/auth-service/application-sit.yml
git commit -m "feat(auth): Update SIT database connection pool"

# Commit environment-wide changes together
git add config-repo/*/application-prod.yml
git commit -m "feat(config): Update all services for production deployment"
```

### 5. Environment Promotion Workflow

When promoting configurations from SIT → QA → PP → Prod:

```bash
# Review differences
diff config-repo/auth-service/application-sit.yml \
     config-repo/auth-service/application-qa.yml

# Copy and modify
cp config-repo/auth-service/application-sit.yml \
   config-repo/auth-service/application-qa.yml

# Update environment-specific values
vim config-repo/auth-service/application-qa.yml
```

---

## Troubleshooting

### Issue: Wrong configuration loaded

**Symptom**: Service uses unexpected configuration values

**Solution**:
1. Verify active profile:
   ```
   INFO --- [main] : The following 1 profile is active: "local"
   ```

2. Check Config Server response:
   ```bash
   curl http://admin:admin123@localhost:8888/auth-service/local | jq '.propertySources[].name'
   ```

3. Verify file loading order (highest priority first):
   ```
   "file:./config-repo/auth-service/application-local.yml"
   "file:./config-repo/application-local.yml"
   "file:./config-repo/auth-service/application.yml"
   "file:./config-repo/application.yml"
   ```

### Issue: Environment variable not applied

**Symptom**: Service uses default value instead of environment variable

**Solution**:
1. Verify environment variable is set:
   ```bash
   echo $DB_HOST  # Linux/Mac
   echo %DB_HOST%  # Windows
   ```

2. Check placeholder syntax in config file:
   ```yaml
   # Correct
   url: jdbc:postgresql://${DB_HOST}:5432/auth_db
   
   # Wrong (missing ${})
   url: jdbc:postgresql://DB_HOST:5432/auth_db
   ```

3. Restart service after setting environment variables

### Issue: Service-specific config not overriding root config

**Symptom**: Root-level config value used instead of service-specific

**Solution**:
1. Verify service-specific file exists:
   ```bash
   ls config-repo/auth-service/application-local.yml
   ```

2. Check property key matches exactly:
   ```yaml
   # Root: application-local.yml
   spring:
     datasource:
       url: jdbc:h2:mem:ecommerce_db
   
   # Service: auth-service/application-local.yml
   spring:
     datasource:
       url: jdbc:h2:mem:auth_db  # Overrides root
   ```

3. Verify YAML indentation (spaces, not tabs)

---

## Migration from Flat Structure

If migrating from flat structure (root-level files only) to service-centric:

### Step 1: Identify Service-Specific Configs

Review root-level environment files and identify service-specific settings:

```yaml
# application-dev.yml (root level)
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/${spring.application.name}  # Generic
```

Vs.

```yaml
# auth-service/application-dev.yml (service-specific)
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_db  # Specific
```

### Step 2: Create Service-Level Environment Files

For each service and environment:

```bash
# Create auth-service environment files
touch config-repo/auth-service/application-local.yml
touch config-repo/auth-service/application-dev.yml
# ... (repeat for all environments)
```

### Step 3: Move Service-Specific Settings

Copy service-specific settings from root files to service files:

```bash
# Extract auth-service settings from root dev config
grep -A 10 "auth" config-repo/application-dev.yml > \
  config-repo/auth-service/application-dev.yml
```

### Step 4: Clean Up Root Files

Remove service-specific settings from root files, keeping only common configs.

### Step 5: Test Configuration Loading

Verify each service loads correct configuration:

```bash
# Test each service + environment combination
curl http://admin:admin123@localhost:8888/auth-service/dev
curl http://admin:admin123@localhost:8888/catalog-service/dev
# ... (test all combinations)
```

---

## Summary

**Configuration Structure**: Service-centric with environment-specific files  
**Total Files**: 40 (8 root + 32 service-specific)  
**Environments**: 7 (local, dev, sit, qa, review, pp, prod)  
**Services**: 4 (auth, catalog, order, gateway)  
**Default Profile**: `local` (H2 in-memory)  
**Production Profile**: `prod` (PostgreSQL/MongoDB with validation)  

**Key Benefits**:
- ✅ Service ownership and autonomy
- ✅ Maximum flexibility per service
- ✅ Clear service boundaries
- ✅ Supports service-oriented teams

**Trade-offs**:
- ⚠️ More files to maintain (40 vs 12)
- ⚠️ Environment-wide changes require editing multiple files
- ⚠️ Potential for configuration drift between services

---

**Last Updated**: August 06, 2026  
**Maintained By**: E-Commerce Platform Team  
**Configuration Version**: 2.0 (Service-Centric Hierarchy)
