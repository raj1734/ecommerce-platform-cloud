# Spring Cloud Config Repository Setup Guide

## Overview

This guide documents the setup and configuration of the centralized configuration repository for the E-Commerce Platform
microservices.

**Two Setup Options**:

1. **Local Directory Setup** (Recommended for development) - Section below
2. **Git Repository Setup** (For production/team collaboration) - See "Git-Based Setup" section

---

## Option 1: Local Directory Setup (Development)

### Overview

For local development, you can use a local directory within your project instead of a Git repository. This approach is
simpler and faster for initial setup and testing.

### Local Directory Structure

Create the following structure inside your `ecommerce-platform` project:

```
ecommerce-platform/
├── config-repo/                       # Local configuration directory
│   ├── application.yml                # Common configuration for all services
│   ├── application-dev.yml            # Development environment overrides
│   ├── application-prod.yml           # Production environment overrides
│   ├── auth-service/
│   │   └── application.yml            # Auth service specific configuration
│   ├── catalog-service/
│   │   └── application.yml            # Catalog service specific configuration
│   ├── order-service/
│   │   └── application.yml            # Order service specific configuration
│   └── gateway-service/
│       └── application.yml            # Gateway service specific configuration
├── config-server/
├── auth-service/
├── catalog-service/
├── order-service/
└── gateway-service/
```

### Setup Steps for Local Directory

#### Step 1: Create Directory Structure

Run these commands from your `ecommerce-platform` project root:

**PowerShell (Windows)**:

```powershell
# Create main config directory
New-Item -ItemType Directory -Path "config-repo" -Force

# Create service subdirectories
New-Item -ItemType Directory -Path "config-repo\auth-service" -Force
New-Item -ItemType Directory -Path "config-repo\catalog-service" -Force
New-Item -ItemType Directory -Path "config-repo\order-service" -Force
New-Item -ItemType Directory -Path "config-repo\gateway-service" -Force
```

**Bash (Linux/Mac)**:

```bash
# Create main config directory
mkdir -p config-repo

# Create service subdirectories
mkdir -p config-repo/auth-service
mkdir -p config-repo/catalog-service
mkdir -p config-repo/order-service
mkdir -p config-repo/gateway-service
```

#### Step 2: Create Configuration Files

Create all configuration files as documented in the "Configuration Files" section below. Place them in the `config-repo`
directory following the structure above.

#### Step 3: Configure Config Server for Local Directory

Update your `config-server/src/main/resources/application.yml`:

```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  profiles:
    active: native  # Use native profile for local file system
  cloud:
    config:
      server:
        native:
          search-locations: file:./config-repo  # Relative path to config directory
          # Or use absolute path:
          # search-locations: file:///C:/path/to/ecommerce-platform/config-repo

management:
  endpoints:
    web:
      exposure:
        include: health,info,refresh
```

**Key Configuration**:

- `spring.profiles.active: native` - Enables local file system mode
- `search-locations: file:./config-repo` - Points to local directory (relative path)
- Use `file:///` prefix for absolute paths on Windows/Linux

#### Step 4: Verify Local Setup

1. **Start Config Server**:
   ```bash
   cd config-server
   mvn spring-boot:run
   ```

2. **Test Configuration Endpoints**:
   ```bash
   # Test common configuration
   curl http://localhost:8888/application/default

   # Test auth-service dev configuration
   curl http://localhost:8888/auth-service/dev

   # Test catalog-service configuration
   curl http://localhost:8888/catalog-service/default
   ```

3. **Verify File Loading**:
   Check Config Server logs for:
   ```
   Adding property source: file:./config-repo/application.yml
   Adding property source: file:./config-repo/auth-service/application.yml
   ```

### Advantages of Local Directory Setup

✅ **Faster Setup**: No Git repository required  
✅ **Immediate Changes**: Edit files and refresh without commits  
✅ **Simpler Workflow**: No Git operations needed  
✅ **Ideal for Development**: Quick iteration and testing  
✅ **No External Dependencies**: Works offline

### Limitations

❌ **No Version Control**: Changes aren't tracked  
❌ **No Collaboration**: Difficult to share with team  
❌ **No Audit Trail**: Can't see configuration history  
❌ **Not Production-Ready**: Should migrate to Git for production

---

## Option 2: Git-Based Setup (Production)

<!-- GIT SETUP - COMMENTED FOR FUTURE USE

When ready to use Git-based configuration management, uncomment this section and follow the steps below.

### Repository Structure

The config repository contains 8 configuration files organized as follows:

```
config-repo/
├── application.yml                    # Common configuration for all services
├── application-dev.yml                # Development environment overrides
├── application-prod.yml               # Production environment overrides
├── auth-service/
│   └── application.yml                # Auth service specific configuration
├── catalog-service/
│   └── application.yml                # Catalog service specific configuration
├── order-service/
│   └── application.yml                # Order service specific configuration
├── gateway-service/
│   └── application.yml                # Gateway service specific configuration
└── README.md                          # Repository documentation
```

-->

---

## Configuration Files

**Note**: The following configuration files are used for BOTH local directory and Git-based setups. Create these files
in your chosen location (`config-repo/` directory for local setup or Git repository for Git-based setup).

### 1. Common Configuration (application.yml)

**Purpose**: Shared configuration across all microservices

**Key Features**:
- Management endpoints configuration
- Prometheus metrics exposure
- Distributed tracing setup
- Logging patterns

**Content**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  level:
    root: INFO
    com.ecommerce: DEBUG

spring:
  application:
    name: ${spring.application.name:default-service}
```

### 2. Development Environment (application-dev.yml)

**Purpose**: Development-specific overrides

**Key Features**:
- Localhost database connections
- Debug logging levels
- Local service URLs

**Content**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/${spring.application.name}
    username: postgres
    password: postgres
  data:
    mongodb:
      uri: mongodb://localhost:27017/${spring.application.name}
  kafka:
    bootstrap-servers: localhost:9092
  redis:
    host: localhost
    port: 6379

logging:
  level:
    root: DEBUG
    com.ecommerce: TRACE
```

### 3. Production Environment (application-prod.yml)

**Purpose**: Production-specific overrides with environment variable placeholders

**Key Features**:
- Environment variable references
- Production-grade connection pools
- Optimized logging levels

**Content**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/${spring.application.name}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  data:
    mongodb:
      uri: mongodb://${MONGO_HOST:localhost}:27017/${spring.application.name}
  kafka:
    bootstrap-servers: ${KAFKA_BROKERS:localhost:9092}
  redis:
    host: ${REDIS_HOST:localhost}
    port: 6379
    password: ${REDIS_PASSWORD:}

logging:
  level:
    root: INFO
    com.ecommerce: INFO
```

### 4. Auth Service Configuration (auth-service/application.yml)

**Purpose**: Authentication and authorization service configuration

**Key Features**:
- PostgreSQL database configuration
- JPA/Hibernate settings
- JWT authentication (24-hour token expiration, 7-day refresh token)
- BCrypt password encoding

**Content**:
```yaml
spring:
  application:
    name: auth-service
  datasource:
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

server:
  port: 8081

jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-change-in-production}
  expiration: 86400000  # 24 hours
  refresh-expiration: 604800000  # 7 days

security:
  password:
    encoder: bcrypt
```

### 5. Catalog Service Configuration (catalog-service/application.yml)

**Purpose**: Product catalog service configuration

**Key Features**:
- MongoDB database configuration
- Redis caching (10-minute product TTL, 30-minute category TTL)
- Cache eviction policies

**Content**:
```yaml
spring:
  application:
    name: catalog-service
  data:
    mongodb:
      database: catalog-db
  cache:
    type: redis
    redis:
      time-to-live: 600000  # 10 minutes

server:
  port: 8082

cache:
  product:
    ttl: 600  # 10 minutes
  category:
    ttl: 1800  # 30 minutes
```

### 6. Order Service Configuration (order-service/application.yml)

**Purpose**: Order management service configuration

**Key Features**:
- PostgreSQL database configuration
- Kafka producer/consumer settings
- Resilience4j circuit breaker for catalog service calls
- Retry patterns with exponential backoff

**Content**:
```yaml
spring:
  application:
    name: order-service
  datasource:
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  kafka:
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: order-service-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"

server:
  port: 8083

resilience4j:
  circuitbreaker:
    instances:
      catalogService:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
        permitted-number-of-calls-in-half-open-state: 3
  retry:
    instances:
      catalogService:
        max-attempts: 3
        wait-duration: 1000
        exponential-backoff-multiplier: 2

kafka:
  topics:
    order-created: order-created-topic
    order-updated: order-updated-topic
```

### 7. Gateway Service Configuration (gateway-service/application.yml)

**Purpose**: API Gateway configuration

**Key Features**:
- Spring Cloud Gateway routes (/api/auth/**, /api/catalog/**, /api/orders/**)
- Redis-based rate limiting
- CORS configuration
- JWT validation filters

**Content**:
```yaml
spring:
  application:
    name: gateway-service
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=1
        - id: catalog-service
          uri: lb://catalog-service
          predicates:
            - Path=/api/catalog/**
          filters:
            - StripPrefix=1
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=1
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "*"
            allowed-methods:
              - GET
              - POST
              - PUT
              - DELETE
            allowed-headers: "*"
            allow-credentials: true

server:
  port: 8080

rate-limiter:
  redis:
    replenish-rate: 10
    burst-capacity: 20

jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-change-in-production}
```

---

## How Configuration Files Are Used in Each Service

This section explains how each microservice consumes and applies the configuration files from the Config Server.

### Configuration Loading Flow

```
Service Startup
   ↓
1. Connect to Config Server (http://localhost:8888)
   ↓
2. Request configuration using service name + active profile
   ↓
3. Config Server merges configurations in hierarchy order
   ↓
4. Service receives merged configuration
   ↓
5. Spring Boot applies configuration to beans
   ↓
6. Service starts with externalized configuration
```

---

### Auth Service (Port 8081)

#### Configuration Files Used

1. **application.yml** (Common base)
2. **application-dev.yml** or **application-prod.yml** (Environment-specific)
3. **auth-service/application.yml** (Service-specific)

#### How It's Used

**Database Configuration**:
```yaml
# From application-dev.yml (Development)
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_db  # Local PostgreSQL
    username: postgres
    password: postgres

# From auth-service/application.yml (Service-specific)
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update  # Auto-create/update tables
    show-sql: true      # Log SQL queries
```

**Result**: Auth Service connects to PostgreSQL at `localhost:5432/auth_db` with Hibernate auto-DDL enabled.

**JWT Configuration**:
```yaml
# From auth-service/application.yml
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-change-in-production}
  expiration: 86400000        # 24 hours (access token)
  refresh-expiration: 604800000  # 7 days (refresh token)
```

**Usage in Code**:
```java
@Configuration
public class JwtConfig {
    @Value("${jwt.secret}")
    private String secret;  // Loaded from Config Server
    
    @Value("${jwt.expiration}")
    private long expiration;  // 86400000 ms
    
    // JWT token generation uses these values
}
```

**Management Endpoints**:
```yaml
# From application.yml (Common)
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

**Result**: Auth Service exposes `/actuator/health`, `/actuator/info`, `/actuator/metrics`, `/actuator/prometheus` endpoints.

---

### Catalog Service (Port 8082)

#### Configuration Files Used

1. **application.yml** (Common base)
2. **application-dev.yml** or **application-prod.yml** (Environment-specific)
3. **catalog-service/application.yml** (Service-specific)

#### How It's Used

**MongoDB Configuration**:
```yaml
# From application-dev.yml (Development)
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/catalog_db  # Local MongoDB

# From catalog-service/application.yml (Service-specific)
spring:
  data:
    mongodb:
      database: catalog_db
      auto-index-creation: true  # Automatically create indexes
```

**Result**: Catalog Service connects to MongoDB at `localhost:27017/catalog_db` with automatic index creation.

**Redis Caching Configuration**:
```yaml
# From application-dev.yml (Development)
spring:
  redis:
    host: localhost
    port: 6379

# From catalog-service/application.yml (Service-specific)
spring:
  cache:
    type: redis
    redis:
      time-to-live: 600000  # 10 minutes default TTL

cache:
  product:
    ttl: 600   # 10 minutes for products
  category:
    ttl: 1800  # 30 minutes for categories
```

**Usage in Code**:
```java
@Service
public class ProductService {
    
    @Cacheable(value = "products", key = "#id")
    public Product getProductById(String id) {
        // Cache expires after 10 minutes (600 seconds)
        return productRepository.findById(id);
    }
    
    @Cacheable(value = "categories", key = "#name")
    public Category getCategoryByName(String name) {
        // Cache expires after 30 minutes (1800 seconds)
        return categoryRepository.findByName(name);
    }
}
```

**Result**: Product queries are cached in Redis for 10 minutes, category queries for 30 minutes.

**Logging Configuration**:
```yaml
# From application.yml (Common)
logging:
  level:
    root: INFO
    com.ecommerce: DEBUG  # Debug level for all ecommerce packages
```

**Result**: Catalog Service logs all `com.ecommerce.catalog.*` classes at DEBUG level.

---

### Order Service (Port 8083)

#### Configuration Files Used

1. **application.yml** (Common base)
2. **application-dev.yml** or **application-prod.yml** (Environment-specific)
3. **order-service/application.yml** (Service-specific)

#### How It's Used

**Database Configuration**:
```yaml
# From application-dev.yml (Development)
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/order_db  # Local PostgreSQL
    username: postgres
    password: postgres

# From order-service/application.yml (Service-specific)
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

**Result**: Order Service connects to PostgreSQL with HikariCP connection pool (max 10 connections).

**Kafka Configuration**:
```yaml
# From application-dev.yml (Development)
spring:
  kafka:
    bootstrap-servers: localhost:9092  # Local Kafka broker

# From order-service/application.yml (Service-specific)
spring:
  kafka:
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
    consumer:
      group-id: order-service-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"

kafka:
  topics:
    order-created: order-created-topic
    order-updated: order-updated-topic
```

**Usage in Code**:
```java
@Service
public class OrderEventPublisher {
    
    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;
    
    @Value("${kafka.topics.order-created}")
    private String orderCreatedTopic;  // "order-created-topic"
    
    public void publishOrderCreated(Order order) {
        OrderEvent event = new OrderEvent(order);
        // Kafka producer uses JSON serialization from config
        kafkaTemplate.send(orderCreatedTopic, event);
    }
}
```

**Result**: Order Service publishes events to Kafka topic `order-created-topic` with JSON serialization.

**Resilience4j Circuit Breaker Configuration**:
```yaml
# From order-service/application.yml
resilience4j:
  circuitbreaker:
    instances:
      catalogService:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
        permitted-number-of-calls-in-half-open-state: 3
  retry:
    instances:
      catalogService:
        max-attempts: 3
        wait-duration: 1000
        exponential-backoff-multiplier: 2
```

**Usage in Code**:
```java
@Service
public class CatalogServiceClient {
    
    @CircuitBreaker(name = "catalogService", fallbackMethod = "getProductFallback")
    @Retry(name = "catalogService")
    public Product getProduct(String productId) {
        // Calls Catalog Service with circuit breaker protection
        // Opens circuit after 50% failure rate over 10 calls
        // Retries 3 times with exponential backoff (1s, 2s, 4s)
        return restTemplate.getForObject(
            "http://catalog-service/api/products/" + productId,
            Product.class
        );
    }
    
    public Product getProductFallback(String productId, Exception e) {
        // Fallback when circuit is open or retries exhausted
        return new Product(productId, "Product Unavailable", 0.0);
    }
}
```

**Result**: Calls to Catalog Service are protected by circuit breaker and retry logic.

---

### Gateway Service (Port 8080)

#### Configuration Files Used

1. **application.yml** (Common base)
2. **application-dev.yml** or **application-prod.yml** (Environment-specific)
3. **gateway-service/application.yml** (Service-specific)

#### How It's Used

**Routing Configuration**:
```yaml
# From gateway-service/application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service  # Load-balanced to auth-service instances
          predicates:
            - Path=/api/auth/**   # Match requests to /api/auth/**
          filters:
            - StripPrefix=1       # Remove /api prefix before forwarding
            
        - id: catalog-service
          uri: lb://catalog-service
          predicates:
            - Path=/api/catalog/**
          filters:
            - StripPrefix=1
            
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=1
```

**How Routing Works**:
```
Client Request: http://localhost:8080/api/auth/login
   ↓
Gateway matches route: id=auth-service, predicate=/api/auth/**
   ↓
Apply StripPrefix=1 filter: /api/auth/login → /auth/login
   ↓
Load balance to auth-service: http://auth-service:8081/auth/login
   ↓
Auth Service processes request
   ↓
Response returned through Gateway to client
```

**Rate Limiting Configuration**:
```yaml
# From application-dev.yml (Development)
spring:
  redis:
    host: localhost
    port: 6379

# From gateway-service/application.yml (Service-specific)
rate-limiter:
  redis:
    replenish-rate: 10   # 10 requests per second
    burst-capacity: 20   # Allow bursts up to 20 requests
```

**Usage in Gateway**:
```java
@Configuration
public class RateLimitConfig {
    
    @Bean
    public RedisRateLimiter redisRateLimiter(
        @Value("${rate-limiter.redis.replenish-rate}") int replenishRate,
        @Value("${rate-limiter.redis.burst-capacity}") int burstCapacity
    ) {
        // Creates rate limiter with 10 req/s, burst 20
        return new RedisRateLimiter(replenishRate, burstCapacity);
    }
}
```

**Result**: Gateway allows 10 requests/second with bursts up to 20, using Redis for distributed rate limiting.

**CORS Configuration**:
```yaml
# From gateway-service/application.yml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "*"
            allowed-methods:
              - GET
              - POST
              - PUT
              - DELETE
            allowed-headers: "*"
            allow-credentials: true
```

**Result**: Gateway allows cross-origin requests from any origin for all routes.

**JWT Validation Configuration**:
```yaml
# From gateway-service/application.yml
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-change-in-production}
```

**Usage in Gateway Filter**:
```java
@Component
public class JwtAuthenticationFilter implements GatewayFilter {
    
    @Value("${jwt.secret}")
    private String jwtSecret;  // Same secret as Auth Service
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = extractToken(exchange.getRequest());
        
        if (token != null && validateToken(token, jwtSecret)) {
            // Token is valid, allow request
            return chain.filter(exchange);
        }
        
        // Token invalid, return 401
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
```

**Result**: Gateway validates JWT tokens using the same secret as Auth Service before forwarding requests.

---

### Config Server (Port 8888)

#### How It Serves Configurations

**Local Directory Mode** (Current Setup):
```yaml
# From config-server/src/main/resources/application.yml
spring:
  profiles:
    active: native  # Use local file system
  cloud:
    config:
      server:
        native:
          search-locations: file:./config-repo  # Load from local directory
```

**Configuration Request Flow**:
```
1. Auth Service starts and requests: http://localhost:8888/auth-service/dev
   ↓
2. Config Server reads files in order:
   - config-repo/application.yml
   - config-repo/application-dev.yml
   - config-repo/auth-service/application.yml
   ↓
3. Config Server merges configurations (later overrides earlier)
   ↓
4. Config Server returns merged JSON to Auth Service
   ↓
5. Auth Service applies configuration and starts
```

**Example Response**:
```json
{
  "name": "auth-service",
  "profiles": ["dev"],
  "propertySources": [
    {
      "name": "file:./config-repo/auth-service/application.yml",
      "source": {
        "server.port": 8081,
        "jwt.secret": "your-256-bit-secret-key",
        "jwt.expiration": 86400000
      }
    },
    {
      "name": "file:./config-repo/application-dev.yml",
      "source": {
        "spring.datasource.url": "jdbc:postgresql://localhost:5432/auth_db",
        "spring.datasource.username": "postgres"
      }
    },
    {
      "name": "file:./config-repo/application.yml",
      "source": {
        "management.endpoints.web.exposure.include": "health,info,metrics,prometheus",
        "logging.level.root": "INFO"
      }
    }
  ]
}
```

---

### Configuration Refresh Without Restart

**Scenario**: You update `config-repo/catalog-service/application.yml` to change cache TTL from 600 to 900 seconds.

**Steps**:

1. **Update Configuration File**:
   ```yaml
   # config-repo/catalog-service/application.yml
   cache:
     product:
       ttl: 900  # Changed from 600 to 900
   ```

2. **Trigger Refresh**:
   ```bash
   # Refresh Catalog Service
   curl -X POST http://localhost:8082/actuator/refresh
   ```

3. **What Happens**:
   ```
   Catalog Service receives refresh request
      ↓
   Fetches latest configuration from Config Server
      ↓
   Config Server reads updated config-repo/catalog-service/application.yml
      ↓
   Catalog Service reloads @RefreshScope beans
      ↓
   New cache TTL (900 seconds) applied without restart
   ```

**Usage in Code**:
```java
@Service
@RefreshScope  // Enables dynamic refresh
public class CacheConfigService {
    
    @Value("${cache.product.ttl}")
    private int productCacheTtl;  // Automatically updated on refresh
    
    public int getProductCacheTtl() {
        return productCacheTtl;  // Returns 900 after refresh
    }
}
```

---

### Environment-Specific Configuration Usage

**Development Environment** (`dev` profile):
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/${spring.application.name}
  kafka:
    bootstrap-servers: localhost:9092
  redis:
    host: localhost

logging:
  level:
    root: DEBUG  # Verbose logging
```

**Startup Command**:
```bash
java -jar auth-service.jar --spring.profiles.active=dev
```

**Production Environment** (`prod` profile):
```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:5432/${spring.application.name}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  kafka:
    bootstrap-servers: ${KAFKA_BROKERS}
  redis:
    host: ${REDIS_HOST}
    password: ${REDIS_PASSWORD}

logging:
  level:
    root: INFO  # Less verbose
```

**Startup Command**:
```bash
DB_USERNAME=prod_user DB_PASSWORD=secure_pass \
java -jar auth-service.jar --spring.profiles.active=prod
```

---

### Configuration Priority Example

**Scenario**: Auth Service in `dev` profile determining database URL

**Configuration Sources**:

1. **application.yml** (Common):
   ```yaml
   # No database configuration
   ```

2. **application-dev.yml** (Environment):
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/auth_db
       username: postgres
       password: postgres
   ```

3. **auth-service/application.yml** (Service-specific):
   ```yaml
   spring:
     datasource:
       driver-class-name: org.postgresql.Driver
   ```

**Merged Result**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_db      # From application-dev.yml
    username: postgres                                  # From application-dev.yml
    password: postgres                                  # From application-dev.yml
    driver-class-name: org.postgresql.Driver           # From auth-service/application.yml
```

**Priority Order** (Highest to Lowest):
1. auth-service/application-dev.yml (if exists) ← **Highest Priority**
2. auth-service/application.yml
3. application-dev.yml
4. application.yml ← **Lowest Priority**

---

### Summary: Configuration Usage by Service

| Service | Key Configurations Used | Source Files |
|---------|------------------------|--------------|
| **Auth Service** | Database (PostgreSQL), JWT secret/expiration, JPA/Hibernate settings | application.yml, application-dev.yml, auth-service/application.yml |
| **Catalog Service** | Database (MongoDB), Redis caching, Cache TTL (products: 10min, categories: 30min) | application.yml, application-dev.yml, catalog-service/application.yml |
| **Order Service** | Database (PostgreSQL), Kafka producer/consumer, Circuit breaker, Retry patterns | application.yml, application-dev.yml, order-service/application.yml |
| **Gateway Service** | Routes (/api/auth/**, /api/catalog/**, /api/orders/**), Rate limiting, CORS, JWT validation | application.yml, application-dev.yml, gateway-service/application.yml |
| **Config Server** | Native file system or Git repository, Search locations | config-server/src/main/resources/application.yml |

---

## Configuration Hierarchy

Spring Cloud Config applies configurations in the following order (later overrides earlier):

1. **application.yml** (Common configuration)
2. **application-{profile}.yml** (Environment-specific: dev, prod)
3. **{service}/application.yml** (Service-specific configuration)
4. **{service}/application-{profile}.yml** (Service + environment specific)

**Example for auth-service in dev environment**:
```
application.yml
  ↓ (overridden by)
application-dev.yml
  ↓ (overridden by)
auth-service/application.yml
  ↓ (overridden by)
auth-service/application-dev.yml (if exists)
```

---

## Verification

### Test Config Server Endpoints

Once your Config Server is running (port 8888), verify configurations:

**For Local Directory Setup**:

```bash
# Test common configuration
curl http://admin:admin123@localhost:8888/application/default

# Test auth-service dev configuration
curl http://admin:admin123@localhost:8888/auth-service/dev

# Test catalog-service prod configuration
curl http://admin:admin123@localhost:8888/catalog-service/prod

# Test order-service configuration
curl http://admin:admin123@localhost:8888/order-service/default

# Test gateway-service configuration
curl http://admin:admin123@localhost:8888/gateway-service/default
```

**Note**: Include Basic Authentication credentials (`admin:admin123`) in the URL or use `-u admin:admin123` flag.

### Expected Response Format

```json
{
  "name": "auth-service",
  "profiles": ["dev"],
  "label": null,
  "version": null,
  "state": null,
  "propertySources": [
    {
      "name": "file:./config-repo/auth-service/application.yml",
      "source": {
        "spring.application.name": "auth-service",
        "server.port": 8081
      }
    },
    {
      "name": "file:./config-repo/application-dev.yml",
      "source": {
        "spring.datasource.url": "jdbc:postgresql://localhost:5432/auth_db"
      }
    },
    {
      "name": "file:./config-repo/application.yml",
      "source": {
        "management.endpoints.web.exposure.include": "health,info,metrics,prometheus"
      }
    }
  ]
}
```

---

## Environment Variables for Production

Set these environment variables in your production deployment:

| Variable | Description | Example |
|----------|-------------|----------|
| `DB_USERNAME` | PostgreSQL username | `ecommerce_user` |
| `DB_PASSWORD` | PostgreSQL password | `secure_password` |
| `DB_HOST` | PostgreSQL host | `prod-db.example.com` |
| `MONGO_HOST` | MongoDB host | `prod-mongo.example.com` |
| `KAFKA_BROKERS` | Kafka broker list | `kafka1:9092,kafka2:9092` |
| `REDIS_HOST` | Redis host | `prod-redis.example.com` |
| `REDIS_PASSWORD` | Redis password | `redis_password` |
| `JWT_SECRET` | JWT signing key | `your-secure-256-bit-key` |

---

## Security Best Practices

### 1. Never Commit Secrets

❌ **Don't do this**:
```yaml
jwt:
  secret: my-actual-production-secret
```

✅ **Do this instead**:
```yaml
jwt:
  secret: ${JWT_SECRET:default-dev-secret}
```

### 2. Use Environment Variables

Always use environment variable placeholders for:
- Database credentials
- API keys
- JWT secrets
- Third-party service credentials

### 3. Encrypt Sensitive Properties

For production, use Spring Cloud Config encryption:

```bash
# Encrypt a value
curl http://localhost:8888/encrypt -d "my-secret-value"

# Use encrypted value in config
jwt:
  secret: '{cipher}AQA...encrypted-value...'
```

### 4. Rotate Secrets Regularly

- Change JWT secrets periodically
- Rotate database passwords
- Update API keys on schedule

---

## Configuration Summary

| Service | Port | Database | Caching | Messaging | Special Features |
|---------|------|----------|---------|-----------|------------------|
| **Auth Service** | 8081 | PostgreSQL | - | - | JWT authentication, BCrypt encoding |
| **Catalog Service** | 8082 | MongoDB | Redis | - | Product/Category caching |
| **Order Service** | 8083 | PostgreSQL | - | Kafka | Circuit breaker, Retry patterns |
| **Gateway Service** | 8080 | - | Redis | - | Rate limiting, CORS, JWT validation |
| **Config Server** | 8888 | - | - | - | Centralized configuration |

---

## Configuration Refresh

To refresh configuration without restarting services:

```bash
# Trigger refresh for a specific service
curl -X POST http://localhost:8081/actuator/refresh

# Or use Spring Cloud Bus for broadcast refresh (if configured)
curl -X POST http://localhost:8888/actuator/bus-refresh
```

---

## Key Takeaways

✅ **Centralized Configuration**: All service configurations managed in one repository  
✅ **Version Control**: Configuration changes tracked via Git  
✅ **Environment Separation**: Dev and prod profiles for different environments  
✅ **Security**: Sensitive values externalized via environment variables  
✅ **Dynamic Refresh**: Configuration updates without service restarts  
✅ **Service-Specific**: Each microservice has dedicated configuration  
✅ **Common Settings**: Shared configuration reduces duplication  
✅ **Resilience**: Circuit breakers and retry patterns configured  

---

## Next Steps

### For Local Directory Setup (Current)

1. ✅ Create `config-repo/` directory in project root
2. ✅ Create all 8 configuration files
3. ✅ Configure Config Server with `native` profile
4. ⬜ Test configuration retrieval for each service
5. ⬜ Start all microservices and verify configuration loading
6. ⬜ (Optional) Migrate to Git-based setup for production

### For Git-Based Setup (Future)

1. ⬜ Create Git repository at https://github.com/raj1734/config-repo
2. ⬜ Push all configuration files to repository
3. ⬜ Update Config Server to use Git profile
4. ⬜ Set up environment variables for production
5. ⬜ Configure encryption for sensitive properties
6. ⬜ Implement configuration refresh mechanism
7. ⬜ Document service-specific configuration changes

---

## Migration Path: Local to Git

When ready to migrate from local directory to Git:

1. **Initialize Git in config-repo**:
   ```bash
   cd config-repo
   git init
   git add .
   git commit -m "Initial configuration"
   ```

2. **Push to Remote Repository**:
   ```bash
   git remote add origin https://github.com/raj1734/config-repo.git
   git push -u origin main
   ```

3. **Update Config Server**:
    - Change `spring.profiles.active` from `native` to `default`
    - Add Git URI configuration
    - Restart Config Server

4. **Verify Git-Based Configuration**:
    - Test all endpoints
    - Check Config Server logs for Git clone messages

---

**Current Setup**: Local Directory (`config-repo/`)  
**Future Setup**: Git Repository (https://github.com/raj1734/config-repo)  
**Last Updated**: August 06, 2026  
**Maintained By**: E-Commerce Platform Team