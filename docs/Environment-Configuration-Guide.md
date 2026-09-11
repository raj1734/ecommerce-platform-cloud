# Environment Configuration Guide

## Overview

This guide explains the multi-environment configuration setup for the E-Commerce Platform. The platform supports 7 different environments, each with specific configurations optimized for its use case.

---

## Available Environments

| Environment | Profile Name | Database | Purpose | H2 Console |
|-------------|-------------|----------|---------|------------|
| **Local** | `local` | H2 In-Memory | Local development with H2 | ✅ Enabled |
| **Development** | `dev` | PostgreSQL | Development with PostgreSQL | ❌ Disabled |
| **SIT** | `sit` | PostgreSQL | System Integration Testing | ❌ Disabled |
| **QA** | `qa` | PostgreSQL | Quality Assurance Testing | ❌ Disabled |
| **Review** | `review` | H2 In-Memory | Code review/feature branches | ✅ Enabled |
| **Pre-Production** | `pp` | PostgreSQL | Production-like testing | ❌ Disabled |
| **Production** | `prod` | PostgreSQL | Live production | ❌ Disabled |

---

## Environment Details

### 1. Local Environment (`local`)

**Purpose**: Quick local development without external database dependencies

**Database**: H2 In-Memory
- **URL**: `jdbc:h2:mem:ecommerce_db`
- **Username**: `sa`
- **Password**: (empty)
- **H2 Console**: Enabled at `/h2-console`

**Characteristics**:
- ✅ No database installation required
- ✅ Fast startup
- ✅ Fresh database on every restart
- ✅ SQL logging enabled
- ✅ Debug logging
- ❌ Data lost on restart

**When to Use**:
- Quick feature development
- Unit testing
- Rapid prototyping
- No PostgreSQL available

**Startup Command**:
```bash
# Default profile (no need to specify)
mvn spring-boot:run

# Or explicitly
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

---

### 2. Development Environment (`dev`)

**Purpose**: Development with persistent PostgreSQL database

**Database**: PostgreSQL
- **URL**: `jdbc:postgresql://localhost:5432/ecommerce_db`
- **Username**: `postgres` (default)
- **Password**: `postgres` (default)
- **DDL**: `update` (auto-update schema)

**Characteristics**:
- ✅ Persistent data storage
- ✅ Production-like database
- ✅ SQL logging enabled
- ✅ Debug logging
- ⚠️ Requires PostgreSQL installation

**When to Use**:
- Development with data persistence
- Testing database migrations
- Integration testing with real database

**Startup Command**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or with environment variable
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

**Prerequisites**:
```bash
# Start PostgreSQL
net start postgresql-x64-14

# Create database
psql -U postgres -c "CREATE DATABASE ecommerce_db;"
```

---

### 3. SIT Environment (`sit`)

**Purpose**: System Integration Testing environment

**Database**: PostgreSQL (remote)
- **URL**: `jdbc:postgresql://sit-db.example.com:5432/ecommerce_db`
- **Username**: Environment variable `DB_USERNAME`
- **Password**: Environment variable `DB_PASSWORD`
- **DDL**: `update` (auto-update schema)

**Characteristics**:
- ✅ Integration testing
- ✅ Shared database server
- ✅ Environment-based configuration
- ⚠️ Requires environment variables

**When to Use**:
- Integration testing across services
- Testing with external systems
- Automated test suites

**Startup Command**:
```bash
# Windows
set DB_USERNAME=ecommerce_user
set DB_PASSWORD=your_password
set MONGO_USERNAME=ecommerce_user
set MONGO_PASSWORD=your_password
set REDIS_PASSWORD=your_password
mvn spring-boot:run -Dspring-boot.run.profiles=sit

# Linux/Mac
export DB_USERNAME=ecommerce_user
export DB_PASSWORD=your_password
export MONGO_USERNAME=ecommerce_user
export MONGO_PASSWORD=your_password
export REDIS_PASSWORD=your_password
mvn spring-boot:run -Dspring-boot.run.profiles=sit
```

---

### 4. QA Environment (`qa`)

**Purpose**: Quality Assurance testing environment

**Database**: PostgreSQL (remote)
- **URL**: `jdbc:postgresql://qa-db.example.com:5432/ecommerce_db`
- **Username**: Environment variable `DB_USERNAME`
- **Password**: Environment variable `DB_PASSWORD`
- **DDL**: `update` (auto-update schema)

**Characteristics**:
- ✅ QA testing
- ✅ Debug logging enabled
- ✅ Environment-based configuration
- ⚠️ Requires environment variables

**When to Use**:
- Manual QA testing
- User acceptance testing
- Bug verification
- Regression testing

**Startup Command**:
```bash
# Windows
set DB_USERNAME=ecommerce_user
set DB_PASSWORD=your_password
mvn spring-boot:run -Dspring-boot.run.profiles=qa

# Linux/Mac
export DB_USERNAME=ecommerce_user
export DB_PASSWORD=your_password
mvn spring-boot:run -Dspring-boot.run.profiles=qa
```

---

### 5. Review Environment (`review`)

**Purpose**: Code review and feature branch testing

**Database**: H2 In-Memory
- **URL**: `jdbc:h2:mem:ecommerce_db`
- **Username**: `sa`
- **Password**: (empty)
- **H2 Console**: Enabled at `/h2-console`

**Characteristics**:
- ✅ No database setup required
- ✅ Fast deployment for PR reviews
- ✅ Isolated testing environment
- ✅ H2 console for data inspection
- ❌ Data lost on restart

**When to Use**:
- Pull request reviews
- Feature branch testing
- Demo environments
- Temporary testing

**Startup Command**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=review
```

---

### 6. Pre-Production Environment (`pp`)

**Purpose**: Production-like testing before deployment

**Database**: PostgreSQL (remote)
- **URL**: `jdbc:postgresql://pp-db.example.com:5432/ecommerce_db`
- **Username**: Environment variable `DB_USERNAME`
- **Password**: Environment variable `DB_PASSWORD`
- **DDL**: `validate` (no schema changes)

**Characteristics**:
- ✅ Production-like configuration
- ✅ Schema validation only
- ✅ Performance testing
- ⚠️ Requires all environment variables

**When to Use**:
- Final testing before production
- Performance testing
- Load testing
- Production deployment rehearsal

**Startup Command**:
```bash
# Windows
set DB_USERNAME=ecommerce_user
set DB_PASSWORD=your_password
set MONGO_USERNAME=ecommerce_user
set MONGO_PASSWORD=your_password
set REDIS_PASSWORD=your_password
mvn spring-boot:run -Dspring-boot.run.profiles=pp

# Linux/Mac
export DB_USERNAME=ecommerce_user
export DB_PASSWORD=your_password
export MONGO_USERNAME=ecommerce_user
export MONGO_PASSWORD=your_password
export REDIS_PASSWORD=your_password
mvn spring-boot:run -Dspring-boot.run.profiles=pp
```

---

### 7. Production Environment (`prod`)

**Purpose**: Live production deployment

**Database**: PostgreSQL (remote)
- **URL**: `jdbc:postgresql://${DB_HOST}:5432/ecommerce_db`
- **Username**: Environment variable `DB_USERNAME` (mandatory)
- **Password**: Environment variable `DB_PASSWORD` (mandatory)
- **DDL**: `validate` (no schema changes)

**Characteristics**:
- ✅ Production-ready configuration
- ✅ Schema validation only
- ✅ Optimized logging (INFO/WARN)
- ✅ Maximum connection pool
- ⚠️ All environment variables mandatory

**When to Use**:
- Live production deployment
- Customer-facing environment

**Startup Command**:
```bash
# Windows
set DB_HOST=prod-db.example.com
set DB_USERNAME=ecommerce_user
set DB_PASSWORD=your_secure_password
set MONGO_HOST=prod-mongo.example.com
set MONGO_USERNAME=ecommerce_user
set MONGO_PASSWORD=your_secure_password
set KAFKA_BROKERS=prod-kafka.example.com:9092
set REDIS_HOST=prod-redis.example.com
set REDIS_PASSWORD=your_secure_password
set JWT_SECRET=your-production-256-bit-secret-key
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Linux/Mac
export DB_HOST=prod-db.example.com
export DB_USERNAME=ecommerce_user
export DB_PASSWORD=your_secure_password
export MONGO_HOST=prod-mongo.example.com
export MONGO_USERNAME=ecommerce_user
export MONGO_PASSWORD=your_secure_password
export KAFKA_BROKERS=prod-kafka.example.com:9092
export REDIS_HOST=prod-redis.example.com
export REDIS_PASSWORD=your_secure_password
export JWT_SECRET=your-production-256-bit-secret-key
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## Configuration Files Structure

```
config-repo/
├── application.yml              # Common configuration for all environments
├── application-local.yml        # Local environment (H2)
├── application-dev.yml          # Development environment (PostgreSQL)
├── application-sit.yml          # SIT environment (PostgreSQL)
├── application-qa.yml           # QA environment (PostgreSQL)
├── application-review.yml       # Review environment (H2)
├── application-pp.yml           # Pre-production environment (PostgreSQL)
├── application-prod.yml         # Production environment (PostgreSQL)
├── auth-service/
│   └── application.yml          # Auth service specific config
├── catalog-service/
│   └── application.yml          # Catalog service specific config
├── order-service/
│   └── application.yml          # Order service specific config
└── gateway-service/
    └── application.yml          # Gateway service specific config
```

---

## Configuration Hierarchy

Spring Cloud Config applies configurations in this order (later overrides earlier):

1. `application.yml` (Common base)
2. `application-{profile}.yml` (Environment-specific)
3. `{service}/application.yml` (Service-specific)
4. `{service}/application-{profile}.yml` (Service + Environment - if exists)

**Example for auth-service with `dev` profile**:

```
1. config-repo/application.yml
2. config-repo/application-dev.yml
3. config-repo/auth-service/application.yml
4. config-repo/auth-service/application-dev.yml (if exists)
```

---

## Environment Variables Reference

### Required for PostgreSQL Environments (dev, sit, qa, pp, prod)

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | `localhost` or `prod-db.example.com` |
| `DB_USERNAME` | Database username | `ecommerce_user` |
| `DB_PASSWORD` | Database password | `secure_password_123` |
| `MONGO_HOST` | MongoDB host | `localhost` or `prod-mongo.example.com` |
| `MONGO_USERNAME` | MongoDB username | `ecommerce_user` |
| `MONGO_PASSWORD` | MongoDB password | `secure_password_123` |
| `KAFKA_BROKERS` | Kafka broker list | `localhost:9092` or `kafka1:9092,kafka2:9092` |
| `REDIS_HOST` | Redis host | `localhost` or `prod-redis.example.com` |
| `REDIS_PASSWORD` | Redis password | `redis_password` |
| `JWT_SECRET` | JWT signing secret (256-bit) | `your-256-bit-secret-key` |

### Optional

| Variable | Description | Default |
|----------|-------------|----------|
| `SPRING_PROFILES_ACTIVE` | Active profile | `local` |

---

## Switching Between Environments

### Method 1: Command Line Argument

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn spring-boot:run -Dspring-boot.run.profiles=sit
mvn spring-boot:run -Dspring-boot.run.profiles=qa
mvn spring-boot:run -Dspring-boot.run.profiles=review
mvn spring-boot:run -Dspring-boot.run.profiles=pp
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Method 2: Environment Variable

```bash
# Windows
set SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run

# Linux/Mac
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

### Method 3: IntelliJ IDEA Run Configuration

1. Edit Run Configuration
2. Add to **VM options**: `-Dspring.profiles.active=dev`
3. Or add to **Environment variables**: `SPRING_PROFILES_ACTIVE=dev`

---

## Testing Configuration Loading

### Verify Active Profile

Check application startup logs:

```
INFO --- [main] c.ecommerce.auth.AuthServiceApplication  : The following 1 profile is active: "local"
```

### Test Config Server

```bash
# Test local profile
curl http://admin:admin123@localhost:8888/auth-service/local

# Test dev profile
curl http://admin:admin123@localhost:8888/auth-service/dev

# Test sit profile
curl http://admin:admin123@localhost:8888/auth-service/sit

# Test qa profile
curl http://admin:admin123@localhost:8888/auth-service/qa

# Test review profile
curl http://admin:admin123@localhost:8888/auth-service/review

# Test pp profile
curl http://admin:admin123@localhost:8888/auth-service/pp

# Test prod profile
curl http://admin:admin123@localhost:8888/auth-service/prod
```

---

## Database Configuration Summary

### H2 In-Memory (local, review)

**Advantages**:
- ✅ No installation required
- ✅ Fast startup
- ✅ Web console for data inspection
- ✅ Perfect for development

**Limitations**:
- ❌ Data lost on restart
- ❌ Not for production

**Access H2 Console**:
- **URL**: http://localhost:8081/h2-console (auth-service)
- **JDBC URL**: `jdbc:h2:mem:auth_db`
- **Username**: `sa`
- **Password**: (empty)

### PostgreSQL (dev, sit, qa, pp, prod)

**Advantages**:
- ✅ Persistent data storage
- ✅ Production-ready
- ✅ Advanced features
- ✅ Scalable

**Requirements**:
- ⚠️ PostgreSQL installation
- ⚠️ Database creation
- ⚠️ Environment variables

---

## Hibernate DDL Strategies

| Environment | DDL Auto | Behavior |
|-------------|----------|----------|
| **local** | `create-drop` | Creates schema on startup, drops on shutdown |
| **dev** | `update` | Updates schema automatically |
| **sit** | `update` | Updates schema automatically |
| **qa** | `update` | Updates schema automatically |
| **review** | `create-drop` | Creates schema on startup, drops on shutdown |
| **pp** | `validate` | Validates schema only, no changes |
| **prod** | `validate` | Validates schema only, no changes |

---

## Logging Levels

| Environment | Root | Spring | Application | SQL |
|-------------|------|--------|-------------|-----|
| **local** | DEBUG | INFO | DEBUG | DEBUG |
| **dev** | DEBUG | INFO | DEBUG | Yes |
| **sit** | INFO | INFO | DEBUG | No |
| **qa** | INFO | INFO | DEBUG | No |
| **review** | INFO | INFO | DEBUG | Yes |
| **pp** | INFO | WARN | INFO | No |
| **prod** | INFO | WARN | INFO | No |

---

## Connection Pool Settings

| Environment | Max Pool Size | Min Idle | Connection Timeout |
|-------------|--------------|----------|-------------------|
| **dev** | 10 | 5 | 30s |
| **sit** | 10 | 5 | 30s |
| **qa** | 10 | 5 | 30s |
| **pp** | 15 | 10 | 30s |
| **prod** | 20 | 10 | 30s |

---

## Best Practices

### Development

1. **Use `local` profile** for quick development without database setup
2. **Use `dev` profile** when you need persistent data
3. **Never commit sensitive credentials** to version control
4. **Use environment variables** for all sensitive data

### Testing

1. **Use `review` profile** for PR reviews and feature branches
2. **Use `sit` profile** for integration testing
3. **Use `qa` profile** for manual testing
4. **Use `pp` profile** for final testing before production

### Production

1. **Always use `prod` profile** for production deployment
2. **Set all environment variables** before startup
3. **Use strong passwords** for all services
4. **Monitor logs** for configuration issues
5. **Never enable H2 console** in production
6. **Use `validate` DDL mode** to prevent accidental schema changes

---

## Troubleshooting

### Issue: Wrong profile active

**Check**:
```bash
# Look for this line in startup logs
INFO --- [main] : The following 1 profile is active: "local"
```

**Solution**:
```bash
# Explicitly set profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Issue: Database connection failed

**For PostgreSQL environments**:
```bash
# Verify PostgreSQL is running
psql -U postgres -h localhost

# Verify database exists
psql -U postgres -l

# Create database if missing
psql -U postgres -c "CREATE DATABASE ecommerce_db;"
```

**For H2 environments**:
- No action needed, H2 is in-memory

### Issue: Config Server not loading configuration

**Check**:
```bash
# Verify Config Server is running
curl http://admin:admin123@localhost:8888/actuator/health

# Test configuration endpoint
curl http://admin:admin123@localhost:8888/auth-service/local
```

### Issue: Environment variables not set

**Solution**:
```bash
# Windows
set DB_USERNAME=ecommerce_user
set DB_PASSWORD=your_password

# Linux/Mac
export DB_USERNAME=ecommerce_user
export DB_PASSWORD=your_password

# Verify
echo %DB_USERNAME%  # Windows
echo $DB_USERNAME   # Linux/Mac
```

---

## Migration Guide

### From Local to Dev

1. Install PostgreSQL
2. Create database: `CREATE DATABASE ecommerce_db;`
3. Set environment variables (optional, defaults exist)
4. Change profile: `-Dspring-boot.run.profiles=dev`

### From Dev to Production

1. Set all required environment variables
2. Update database hosts to production servers
3. Use strong passwords
4. Change profile: `-Dspring-boot.run.profiles=prod`
5. Verify schema with `validate` DDL mode

---

## Summary

✅ **7 environments** configured (local, dev, sit, qa, review, pp, prod)  
✅ **H2 for local/review** - No database setup required  
✅ **PostgreSQL for others** - Production-ready persistent storage  
✅ **Environment variables** - Secure configuration management  
✅ **Profile-based activation** - Easy environment switching  
✅ **Config Server integration** - Centralized configuration  
✅ **Default profile: local** - Works out of the box  

---

**Default Profile**: `local` (H2 In-Memory)  
**Recommended for Development**: `local` (quick) or `dev` (persistent)  
**Recommended for Testing**: `sit`, `qa`, `review`  
**Recommended for Production**: `pp` (testing), `prod` (live)  

