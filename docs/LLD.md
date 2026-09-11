# Low-Level Design (LLD) - E-Commerce Microservices Platform

## 1. Introduction

### 1.1 Purpose

This Low-Level Design (LLD) document provides comprehensive technical specifications for the E-Commerce Microservices
Platform. It serves as the definitive guide for developers, architects, and DevOps engineers implementing, maintaining,
and extending the system.

### 1.2 Scope

The document covers:

- Detailed microservice architecture and implementation
- Database schemas and data models
- API specifications and contracts
- Sequence diagrams for critical workflows
- Class diagrams for domain models
- Infrastructure and deployment configurations
- Security, caching, and messaging implementations
- Exception handling and logging strategies

### 1.3 System Context

The platform is a distributed e-commerce system built using microservices architecture, implementing:

- **API Gateway Pattern**: Centralized routing and security via Spring Cloud Gateway
- **Database-per-Service**: PostgreSQL for transactional services, MongoDB for catalog
- **Event-Driven Communication**: Apache Kafka for asynchronous messaging
- **Resilience Patterns**: Circuit breakers, retries, and fallbacks using Resilience4j
- **Caching Strategy**: Redis for performance optimization
- **Cloud-Native Deployment**: AWS ECS Fargate with Terraform/CloudFormation IaC

### 1.4 Technology Stack

| Component | Technology | Version |
|-----------|------------|----------|
| Framework | Spring Boot | 3.2.0 |
| Cloud | Spring Cloud | 2023.0.0 |
| Language | Java | 17 |
| Gateway | Spring Cloud Gateway | 4.1.0 |
| Config | Spring Cloud Config | 4.1.0 |
| Auth Database | PostgreSQL | 15.4 |
| Order Database | PostgreSQL | 15.4 |
| Catalog Database | MongoDB | 7.0 |
| Cache | Redis | 7.2 |
| Message Broker | Apache Kafka | 3.5.1 |
| Resilience | Resilience4j | 2.1.0 |
| Tracing | Micrometer Tracing + Brave | 1.2.0 |
| Metrics | Prometheus | 2.47 |
| Visualization | Grafana | 10.2 |
| Containerization | Docker | 24.0 |
| Orchestration | AWS ECS Fargate | - |
| IaC | Terraform / CloudFormation | 1.6 / - |

### 1.5 Document Conventions

- **Code Blocks**: Syntax-highlighted Java, YAML, SQL, or JSON
- **Diagrams**: Mermaid notation for sequence and class diagrams
- **API Specs**: OpenAPI 3.0 format
- **Configuration**: YAML format for Spring Boot properties

---

## 2. Project Structure

### 2.1 Maven Multi-Module Project

The e-commerce platform is structured as a **Maven multi-module project** with a parent POM at the root level that
manages all microservices. This architecture provides centralized dependency management, consistent build configuration,
and simplified multi-module builds.

**Parent POM Benefits:**

- Centralized version management for all dependencies
- Consistent Java 17 compilation across all services
- Shared plugin configurations (Spring Boot, Compiler, Surefire, Failsafe)
- Single-command builds for all microservices (`mvn clean install`)
- DRY principle for common dependencies (Actuator, Prometheus, Tracing, Lombok)

### 2.2 Monorepo Layout

```
ecommerce-platform/
├── pom.xml                       # ✨ Parent POM (Maven multi-module)
├── gateway-service/              # API Gateway (Port 8080)
│   ├── src/main/java/com/ecommerce/gateway/
│   │   ├── filter/
│   │   │   └── JwtAuthenticationFilter.java
│   │   ├── config/
│   │   │   ├── RedisConfig.java
│   │   │   └── CorsConfig.java
│   │   └── GatewayServiceApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── auth-service/                 # Authentication Service (Port 8081)
│   ├── src/main/java/com/ecommerce/auth/
│   │   ├── controller/
│   │   │   └── AuthController.java
│   │   ├── service/
│   │   │   └── AuthService.java
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   ├── entity/
│   │   │   └── User.java
│   │   ├── dto/
│   │   │   ├── RegisterRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   └── AuthResponse.java
│   │   ├── security/
│   │   │   └── JwtUtil.java
│   │   └── AuthServiceApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── catalog-service/              # Product Catalog Service (Port 8082)
│   ├── src/main/java/com/ecommerce/catalog/
│   │   ├── controller/
│   │   │   └── ProductController.java
│   │   ├── service/
│   │   │   └── ProductService.java
│   │   ├── repository/
│   │   │   └── ProductRepository.java
│   │   ├── document/
│   │   │   └── Product.java
│   │   ├── dto/
│   │   │   ├── ProductRequest.java
│   │   │   └── ProductResponse.java
│   │   ├── config/
│   │   │   ├── MongoConfig.java
│   │   │   └── CacheConfig.java
│   │   └── CatalogServiceApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── order-service/                # Order & Cart Service (Port 8083)
│   ├── src/main/java/com/ecommerce/order/
│   │   ├── controller/
│   │   │   └── OrderController.java
│   │   ├── service/
│   │   │   └── OrderService.java
│   │   ├── repository/
│   │   │   ├── OrderRepository.java
│   │   │   └── OrderItemRepository.java
│   │   ├── entity/
│   │   │   ├── Order.java
│   │   │   └── OrderItem.java
│   │   ├── dto/
│   │   │   ├── OrderRequest.java
│   │   │   ├── OrderResponse.java
│   │   │   └── OrderItemRequest.java
│   │   ├── client/
│   │   │   └── CatalogClient.java
│   │   ├── event/
│   │   │   └── OrderCreatedEvent.java
│   │   ├── config/
│   │   │   ├── FeignConfig.java
│   │   │   └── KafkaProducerConfig.java
│   │   └── OrderServiceApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── config-server/                # Spring Cloud Config Server (Port 8888)
│   ├── src/main/java/com/ecommerce/config/
│   │   └── ConfigServerApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── terraform/                    # Terraform Infrastructure as Code
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── modules/
│       ├── vpc/
│       ├── security-groups/
│       ├── rds/
│       ├── msk/
│       └── ecs/
│
├── cloudformation/               # CloudFormation Infrastructure (Alternative)
│   ├── main.yaml
│   ├── parameters-example.json
│   ├── deploy.sh
│   ├── deploy.ps1
│   └── templates/
│       ├── vpc.yaml
│       ├── security-groups.yaml
│       ├── rds.yaml
│       ├── msk.yaml
│       └── ecs.yaml
│
├── monitoring/                   # Observability Stack
│   ├── prometheus/
│   │   └── prometheus.yml
│   └── grafana/
│       ├── provisioning/
│       │   ├── datasources/
│       │   │   └── prometheus.yml
│       │   └── dashboards/
│       │       ├── dashboard.yml
│       │       └── microservices-dashboard.json
│       └── grafana.ini
│
├── docs/                         # Documentation
│   ├── HLD.md
│   ├── LLD.md
│   └── API-Specs/
│       ├── auth-service-openapi.yaml
│       ├── catalog-service-openapi.yaml
│       └── order-service-openapi.yaml
│
├── docker-compose.yml            # Local Development Stack
├── pom.xml                       # Parent POM for multi-module project
└── README.md                     # Project Documentation
```

### 2.3 Parent POM Configuration

**File:** `pom.xml` (root directory)  
**Packaging:** `pom`  
**Group ID:** `com.ecommerce`  
**Artifact ID:** `ecommerce-platform`  
**Version:** `1.0.0-SNAPSHOT`

#### Modules Defined

```xml
<modules>
    <module>gateway-service</module>
    <module>auth-service</module>
    <module>catalog-service</module>
    <module>order-service</module>
    <module>config-server</module>
</modules>
```

#### Centralized Version Management

| Component | Property | Version |
|-----------|----------|---------|
| Java | `java.version` | 17 |
| Spring Boot | `spring-boot.version` | 3.2.0 |
| Spring Cloud | `spring-cloud.version` | 2023.0.0 |
| PostgreSQL Driver | `postgresql.version` | 42.7.1 |
| MongoDB Driver | `mongodb.version` | 4.11.1 |
| JWT (JJWT) | `jjwt.version` | 0.12.3 |
| Resilience4j | `resilience4j.version` | 2.1.0 |
| Micrometer Tracing | `micrometer-tracing.version` | 1.2.0 |
| Zipkin Reporter | `zipkin-reporter.version` | 2.16.4 |
| Testcontainers | `testcontainers.version` | 1.19.3 |

#### Common Dependencies (Inherited by All Modules)

The parent POM defines common dependencies that are automatically included in all 5 microservices:

**Observability & Monitoring:**

- Spring Boot Actuator - Health checks, metrics, info endpoints
- Micrometer Prometheus Registry - Metrics export to Prometheus
- Micrometer Tracing Bridge (Brave) - Distributed tracing
- Zipkin Reporter - Trace export to Zipkin

**Developer Productivity:**

- Lombok - Reduces boilerplate code
- Spring Boot DevTools - Hot reload during development
- Spring Boot Configuration Processor - IDE metadata

**Testing:**

- Spring Boot Starter Test - JUnit 5, Mockito, AssertJ

#### Dependency Management (BOM Imports)

```xml
<dependencyManagement>
    <dependencies>
        <!-- Spring Boot Dependencies BOM -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring-boot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        
        <!-- Spring Cloud Dependencies BOM -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        
        <!-- Resilience4j BOM -->
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-bom</artifactId>
            <version>${resilience4j.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### Standardized Plugin Configuration

**Spring Boot Maven Plugin:**

- Creates executable JAR files for all services
- Excludes Lombok from final JAR (compile-time only)
- Consistent packaging across all microservices

**Maven Compiler Plugin:**

- Java 17 compilation for all services
- UTF-8 encoding for all source files
- Lombok annotation processing configured

**Maven Surefire Plugin (Unit Tests):**

- Runs all `*Test.java` and `*Tests.java` files
- Consistent test execution across all services

**Maven Failsafe Plugin (Integration Tests):**

- Runs all `*IT.java` and `*IntegrationTest.java` files
- Separate phase for integration tests (`mvn verify`)

**Dockerfile Maven Plugin:**

- Build Docker images with `mvn dockerfile:build`
- Consistent image naming: `ecommerce/{service-name}:1.0.0-SNAPSHOT`

#### Multi-Module Build Commands

```bash
# Build all services from root directory
mvn clean install

# Run tests for all services
mvn test

# Run integration tests
mvn verify

# Package all services (creates executable JARs)
mvn package

# Build specific service
mvn clean install -pl auth-service

# Build service with dependencies
mvn clean install -pl auth-service -am

# Skip tests
mvn clean install -DskipTests

# Build Docker images for all services
mvn dockerfile:build
```

#### Child POM Structure

Each microservice's `pom.xml` references the parent:

```xml
<parent>
    <groupId>com.ecommerce</groupId>
    <artifactId>ecommerce-platform</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>

<artifactId>auth-service</artifactId>
<name>Auth Service</name>
<description>Authentication and Authorization Service</description>

<dependencies>
    <!-- Service-specific dependencies WITHOUT versions -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <!-- Version inherited from parent -->
    </dependency>
    <!-- Common dependencies inherited from parent -->
</dependencies>
```

### 2.4 Service Dependency Graph

```mermaid
graph TB
    Gateway[Gateway Service]
    Auth[Auth Service]
    Catalog[Catalog Service]
    Order[Order Service]
    Config[Config Server]
    
    Gateway -->|JWT Validation| Auth
    Gateway -->|Routes| Catalog
    Gateway -->|Routes| Order
    Order -->|Feign Client| Catalog
    
    Auth -->|Reads Config| Config
    Catalog -->|Reads Config| Config
    Order -->|Reads Config| Config
    Gateway -->|Reads Config| Config
    
    Auth -->|PostgreSQL| AuthDB[(Auth DB)]
    Order -->|PostgreSQL| OrderDB[(Order DB)]
    Catalog -->|MongoDB| CatalogDB[(Catalog DB)]
    
    Gateway -->|Rate Limiting| Redis[(Redis)]
    Catalog -->|Caching| Redis
    
    Order -->|Publishes Events| Kafka[Apache Kafka]
```

---

## 3. Microservice Details

### 3.1 API Gateway

#### Overview

The API Gateway serves as the single entry point for all client requests, implementing cross-cutting concerns like
authentication, rate limiting, routing, and CORS.

**Port**: 8080  
**Technology**: Spring Cloud Gateway  
**Key Responsibilities**:

- JWT token validation
- Request routing to downstream services
- Rate limiting (Redis-backed)
- CORS configuration
- User context propagation
- Centralized logging and tracing

#### Components
```
GatewayServiceApplication
├── filter/
│   └── JwtAuthenticationFilter.java
├── config/
│   └── GatewayConfig.java (implicit via application.yml)
└── application.yml
```

#### JWT Authentication Filter
```java
Class: JwtAuthenticationFilter
Purpose: Validate JWT tokens and propagate user context

Flow:
1. Extract Authorization header
2. Validate Bearer token format
3. Parse JWT using JJWT library
4. Extract claims (userId, email)
5. Add custom headers (X-User-Id, X-User-Email)
6. Forward request to downstream service

Fallback: Return 401 Unauthorized on validation failure
```

#### Route Configuration
```yaml
Routes:
  - /api/auth/** → auth-service (rate limit: 10/s)
  - /api/catalog/** → catalog-service (rate limit: 50/s)
  - /api/orders/** → order-service (rate limit: 20/s)

Filters:
  - StripPrefix: Remove /api/{service} prefix
  - RequestRateLimiter: Redis-backed token bucket
  - CORS: Allow all origins (configurable)
```

### 3.2 Auth Service

#### Overview

The Auth Service manages user registration, authentication, and JWT token lifecycle.

**Port**: 8081  
**Database**: PostgreSQL (auth_db)  
**Key Responsibilities**:

- User registration with BCrypt password hashing
- User authentication and JWT token generation
- Token validation for gateway
- User profile management

#### Entity Model
```java
Entity: User
Table: users

Fields:
- id: Long (PK, auto-increment)
- email: String (unique, not null)
- password: String (BCrypt hashed, not null)
- firstName: String
- lastName: String
- role: String (default: "USER")
- active: Boolean (default: true)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

Indexes:
- email (unique)
```

#### Service Layer
```java
Class: AuthService

Methods:
1. register(RegisterRequest) → AuthResponse
   - Validate email uniqueness
   - Hash password (BCrypt)
   - Save user to database
   - Generate JWT token
   - Return token + user info

2. login(LoginRequest) → AuthResponse
   - Find user by email
   - Validate password
   - Generate JWT token
   - Return token + user info

3. validateToken(String token) → Boolean
   - Parse JWT
   - Verify signature
   - Check expiration
   - Return validation result
```

#### JWT Utility
```java
Class: JwtUtil

Methods:
1. generateToken(User) → String
   - Create claims (email, role)
   - Set subject (userId)
   - Set expiration (24 hours)
   - Sign with HMAC-SHA256
   - Return token string

2. validateToken(String) → Boolean
   - Parse token
   - Verify signature
   - Check expiration
   - Return true/false

3. extractClaims(String) → Claims
   - Parse token
   - Return claims object
```

### 3.3 Catalog Service

#### Overview

The Catalog Service manages the product catalog with high-performance caching.

**Port**: 8082  
**Database**: MongoDB (products collection)  
**Cache**: Redis (Cache-Aside pattern)  
**Key Responsibilities**:

- Product CRUD operations
- Category-based product retrieval
- Redis caching for read-heavy operations
- Search functionality

#### Entity Model
```java
Document: Product
Collection: products

Fields:
- id: String (MongoDB ObjectId)
- name: String
- description: String
- price: BigDecimal
- stock: Integer
- category: String
- imageUrl: String
- active: Boolean (default: true)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

Indexes:
- category
- name (text index for search)
```

#### Caching Strategy
```java
Class: ProductService

Cache Configuration:
- Cache Name: "products"
- TTL: 10 minutes
- Eviction: LRU
- Serialization: JSON

Cached Methods:
1. getAllProducts() → @Cacheable(key="'all'")
2. getProductById(id) → @Cacheable(key="#id")
3. getProductsByCategory(cat) → @Cacheable(key="'category:' + #cat")

Cache Eviction:
- createProduct() → @CacheEvict(allEntries=true)
- updateProduct() → @CacheEvict(allEntries=true)
- deleteProduct() → @CacheEvict(allEntries=true)
```

#### Redis Configuration
```java
Class: CacheConfig

Configuration:
- Serializer: GenericJackson2JsonRedisSerializer
- Key Serializer: StringRedisSerializer
- TTL: 10 minutes
- Cache Manager: RedisCacheManager
```

### 3.4 Order & Cart Service

#### Overview

The Order Service handles order creation, management, and integrates with Catalog Service for product validation.

**Port**: 8083  
**Database**: PostgreSQL (order_db)  
**Integrations**: Catalog Service (Feign), Kafka (Event Publishing)  
**Key Responsibilities**:

- Order creation and management
- Cart operations
- Product validation via Feign client
- Resilience patterns (Circuit Breaker, Retry)
- Kafka event publishing for order lifecycle

#### Entity Model
```java
Entity: Order
Table: orders

Fields:
- id: Long (PK, auto-increment)
- userId: Long
- userEmail: String
- items: List<OrderItem> (OneToMany)
- totalAmount: BigDecimal
- status: OrderStatus (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

Entity: OrderItem
Table: order_items

Fields:
- id: Long (PK, auto-increment)
- order: Order (ManyToOne)
- productId: String
- productName: String
- price: BigDecimal
- quantity: Integer
- subtotal: BigDecimal
```

#### Feign Client
```java
Interface: CatalogClient

Configuration:
- Name: catalog-service
- URL: http://localhost:8082 (configurable)

Methods:
1. getProductById(String id) → ProductResponse
   - Endpoint: GET /api/products/{id}
   - Timeout: 5 seconds
   - Circuit Breaker: catalogService
```

#### Circuit Breaker Configuration
```yaml
resilience4j.circuitbreaker:
  instances:
    catalogService:
      slidingWindowSize: 10
      minimumNumberOfCalls: 5
      failureRateThreshold: 50%
      waitDurationInOpenState: 5s
      permittedNumberOfCallsInHalfOpenState: 3
```

#### Service Layer
```java
Class: OrderService

Methods:
1. createOrder(OrderRequest, userId, userEmail) → Order
   Flow:
   - Create order entity
   - For each item:
     * Fetch product from catalog (with circuit breaker)
     * Create order item
     * Calculate subtotal
   - Calculate total amount
   - Save order to database
   - Publish OrderCreated event to Kafka
   - Return saved order

2. getProductWithResilience(productId) → ProductResponse
   Annotations:
   - @CircuitBreaker(name="catalogService", fallback="getProductFallback")
   - @Retry(name="catalogService")
   
   Fallback:
   - Return default product with zero price
   - Log circuit breaker activation
```

#### Kafka Event Publishing
```java
Class: OrderService

Event: OrderCreatedEvent
Topic: order-created

Payload:
{
  "orderId": 123,
  "userId": 456,
  "userEmail": "user@example.com",
  "totalAmount": 99.99,
  "createdAt": "2026-08-03T10:00:00Z"
}

Configuration:
- Serializer: JsonSerializer
- Acks: 1 (leader acknowledgment)
- Retries: 3
```

### 3.5 Config Server

#### Overview

The Config Server provides centralized, externalized configuration management for all microservices.

**Port**: 8888  
**Backend**: Git repository  
**Security**: Basic Authentication  
**Key Responsibilities**:

- Centralized configuration management
- Environment-specific properties
- Dynamic configuration refresh
- Encrypted sensitive properties

#### Configuration
```yaml
Git Backend:
- URI: https://github.com/your-org/config-repo
- Branch: main
- Search Paths: {application}/

Security:
- Type: Basic Authentication
- Username: admin
- Password: admin123 (environment variable)

Endpoints:
- /{application}/{profile}
- /{application}/{profile}/{label}
```

---

## 4. Database Design

### 4.1 Auth Database (PostgreSQL)

**Database Name**: auth_db  
**Engine**: PostgreSQL 15.4  
**Instance Type**: db.t3.micro (AWS RDS)  
**Storage**: 20GB gp3  
**Backup Retention**: 7 days

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
```

### 4.2 Order Database (PostgreSQL)

**Database Name**: order_db  
**Engine**: PostgreSQL 15.4  
**Instance Type**: db.t3.micro (AWS RDS)  
**Storage**: 20GB gp3  
**Backup Retention**: 7 days

```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id VARCHAR(255) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
```

### 4.3 Catalog Database (MongoDB)

**Database Name**: catalog_db  
**Engine**: MongoDB 7.0  
**Deployment**: Docker container (local) / MongoDB Atlas (production)  
**Replication**: Replica Set (production)

```javascript
db.products.createIndex({ "category": 1 });
db.products.createIndex({ "name": "text" });
db.products.createIndex({ "active": 1 });

Document Structure:
{
  "_id": ObjectId("..."),
  "name": "Product Name",
  "description": "Product Description",
  "price": NumberDecimal("99.99"),
  "stock": 100,
  "category": "Electronics",
  "imageUrl": "https://...",
  "active": true,
  "createdAt": ISODate("2026-08-03T10:00:00Z"),
  "updatedAt": ISODate("2026-08-03T10:00:00Z")
}
```

### 4.4 Entity Relationship Diagram

```mermaid
erDiagram
    USER ||--o{ ORDER : places
    ORDER ||--|{ ORDER_ITEM : contains
    
    USER {
        bigint id PK
        varchar email UK
        varchar password
        varchar first_name
        varchar last_name
        varchar role
        boolean active
        timestamp created_at
        timestamp updated_at
    }
    
    ORDER {
        bigint id PK
        bigint user_id FK
        varchar user_email
        decimal total_amount
        varchar status
        timestamp created_at
        timestamp updated_at
    }
    
    ORDER_ITEM {
        bigint id PK
        bigint order_id FK
        varchar product_id
        varchar product_name
        decimal price
        integer quantity
        decimal subtotal
    }
```

---

## 5. API Specifications

### 5.1 Auth Service API

**Base URL**: `http://localhost:8081/api/auth` (local) | `https://api.example.com/api/auth` (production)

#### POST /api/auth/register
```json
Request:
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}

Response (201):
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "user@example.com",
  "role": "USER"
}
```

#### POST /api/auth/login
```json
Request:
{
  "email": "user@example.com",
  "password": "password123"
}

Response (200):
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "user@example.com",
  "role": "USER"
}
```

#### POST /api/auth/validate

```json
Request:
Headers:
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

Response (200):
{
  "valid": true,
  "userId": 123,
  "email": "user@example.com",
  "role": "USER"
}

Response (401):
{
  "valid": false,
  "error": "Invalid or expired token"
}
```

### 5.2 Catalog Service API

**Base URL**: `http://localhost:8082/api/products` (local) | `https://api.example.com/api/products` (production)

#### GET /api/products
```json
Response (200):
[
  {
    "id": "64f1a2b3c4d5e6f7g8h9i0j1",
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99,
    "stock": 50,
    "category": "Electronics",
    "imageUrl": "https://...",
    "active": true
  }
]
```

#### POST /api/products
```json
Request:
{
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 999.99,
  "stock": 50,
  "category": "Electronics",
  "imageUrl": "https://..."
}

Response (201):
{
  "id": "64f1a2b3c4d5e6f7g8h9i0j1",
  "name": "Laptop",
  ...
}
```

#### GET /api/products/{id}

```json
Response (200):
{
  "id": "64f1a2b3c4d5e6f7g8h9i0j1",
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 999.99,
  "stock": 50,
  "category": "Electronics",
  "imageUrl": "https://...",
  "active": true,
  "createdAt": "2026-08-01T10:00:00Z",
  "updatedAt": "2026-08-01T10:00:00Z"
}

Response (404):
{
  "error": "Product not found",
  "productId": "64f1a2b3c4d5e6f7g8h9i0j1"
}
```

#### PUT /api/products/{id}

```json
Request:
{
  "name": "Updated Laptop",
  "description": "Updated description",
  "price": 899.99,
  "stock": 45,
  "category": "Electronics",
  "imageUrl": "https://..."
}

Response (200):
{
  "id": "64f1a2b3c4d5e6f7g8h9i0j1",
  "name": "Updated Laptop",
  ...
}
```

#### DELETE /api/products/{id}

```json
Response (204): No Content

Response (404):
{
  "error": "Product not found",
  "productId": "64f1a2b3c4d5e6f7g8h9i0j1"
}
```

### 5.3 Order Service API

**Base URL**: `http://localhost:8083/api/orders` (local) | `https://api.example.com/api/orders` (production)

#### POST /api/orders
```json
Request:
Headers:
  X-User-Id: 123
  X-User-Email: user@example.com

Body:
{
  "items": [
    {
      "productId": "64f1a2b3c4d5e6f7g8h9i0j1",
      "quantity": 2
    }
  ]
}

Response (201):
{
  "id": 1,
  "userId": 123,
  "userEmail": "user@example.com",
  "items": [
    {
      "id": 1,
      "productId": "64f1a2b3c4d5e6f7g8h9i0j1",
      "productName": "Laptop",
      "price": 999.99,
      "quantity": 2,
      "subtotal": 1999.98
    }
  ],
  "totalAmount": 1999.98,
  "status": "CONFIRMED",
  "createdAt": "2026-08-03T10:00:00Z"
}
```

#### GET /api/orders/{id}

```json
Request:
Headers:
  X-User-Id: 123
  X-User-Email: user@example.com

Response (200):
{
  "id": 1,
  "userId": 123,
  "userEmail": "user@example.com",
  "items": [
    {
      "id": 1,
      "productId": "64f1a2b3c4d5e6f7g8h9i0j1",
      "productName": "Laptop",
      "price": 999.99,
      "quantity": 2,
      "subtotal": 1999.98
    }
  ],
  "totalAmount": 1999.98,
  "status": "CONFIRMED",
  "createdAt": "2026-08-03T10:00:00Z",
  "updatedAt": "2026-08-03T10:00:00Z"
}

Response (404):
{
  "error": "Order not found",
  "orderId": 1
}
```

#### GET /api/orders/user/{userId}

```json
Request:
Headers:
  X-User-Id: 123
  X-User-Email: user@example.com

Response (200):
[
  {
    "id": 1,
    "userId": 123,
    "totalAmount": 1999.98,
    "status": "CONFIRMED",
    "createdAt": "2026-08-03T10:00:00Z"
  },
  {
    "id": 2,
    "userId": 123,
    "totalAmount": 499.99,
    "status": "DELIVERED",
    "createdAt": "2026-08-02T15:30:00Z"
  }
]
```

---

## 6. Internal Sequence Diagrams

### 6.1 User Registration Flow

```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant Auth
    participant AuthDB
    
    Client->>Gateway: POST /api/auth/register
    Gateway->>Auth: Forward request
    Auth->>AuthDB: Check email uniqueness
    AuthDB-->>Auth: Email available
    Auth->>Auth: Hash password (BCrypt)
    Auth->>AuthDB: INSERT user
    AuthDB-->>Auth: User created
    Auth->>Auth: Generate JWT token
    Auth-->>Gateway: 201 Created + token
    Gateway-->>Client: 201 Created + token
```

### 6.2 User Login Flow

```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant Auth
    participant AuthDB
    
    Client->>Gateway: POST /api/auth/login
    Gateway->>Auth: Forward request
    Auth->>AuthDB: SELECT user by email
    AuthDB-->>Auth: User record
    Auth->>Auth: Validate password (BCrypt)
    Auth->>Auth: Generate JWT token
    Auth-->>Gateway: 200 OK + token
    Gateway-->>Client: 200 OK + token
```

### 6.3 Product Retrieval with Caching

```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant Catalog
    participant Redis
    participant MongoDB
    
    Client->>Gateway: GET /api/catalog/products/{id}
    Gateway->>Gateway: Validate JWT
    Gateway->>Catalog: Forward request
    Catalog->>Redis: GET product:{id}
    alt Cache Hit
        Redis-->>Catalog: Product data
        Catalog-->>Gateway: 200 OK + product
    else Cache Miss
        Redis-->>Catalog: null
        Catalog->>MongoDB: findById(id)
        MongoDB-->>Catalog: Product document
        Catalog->>Redis: SET product:{id} (TTL 10min)
        Catalog-->>Gateway: 200 OK + product
    end
    Gateway-->>Client: 200 OK + product
```

### 6.4 Order Creation with Circuit Breaker

```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant Order
    participant Catalog
    participant OrderDB
    participant Kafka
    
    Client->>Gateway: POST /api/orders
    Gateway->>Gateway: Validate JWT + Extract user context
    Gateway->>Order: Forward request + X-User-Id header
    Order->>Order: Create Order entity
    
    loop For each item
        Order->>Catalog: GET /api/products/{id} (Feign + Circuit Breaker)
        alt Catalog Available
            Catalog-->>Order: Product data
        else Circuit Open
            Order->>Order: Fallback: Default product
        end
        Order->>Order: Create OrderItem + Calculate subtotal
    end
    
    Order->>Order: Calculate total amount
    Order->>OrderDB: INSERT order + order_items
    OrderDB-->>Order: Order saved
    Order->>Kafka: Publish OrderCreatedEvent
    Kafka-->>Order: Ack
    Order-->>Gateway: 201 Created + order
    Gateway-->>Client: 201 Created + order
```

### 6.5 JWT Validation at Gateway

```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant JwtFilter
    participant Downstream
    
    Client->>Gateway: Request + Authorization: Bearer {token}
    Gateway->>JwtFilter: Extract token
    JwtFilter->>JwtFilter: Validate signature
    JwtFilter->>JwtFilter: Check expiration
    alt Valid Token
        JwtFilter->>JwtFilter: Extract claims (userId, email)
        JwtFilter->>Gateway: Add X-User-Id, X-User-Email headers
        Gateway->>Downstream: Forward request + headers
        Downstream-->>Gateway: Response
        Gateway-->>Client: Response
    else Invalid Token
        JwtFilter-->>Client: 401 Unauthorized
    end
```

---

## 7. Class Diagrams

### 7.1 Auth Service Domain Model

```mermaid
classDiagram
    class User {
        -Long id
        -String email
        -String password
        -String firstName
        -String lastName
        -String role
        -Boolean active
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +getId() Long
        +getEmail() String
        +setPassword(String) void
    }
    
    class AuthController {
        -AuthService authService
        +register(RegisterRequest) ResponseEntity~AuthResponse~
        +login(LoginRequest) ResponseEntity~AuthResponse~
        +validate(String) ResponseEntity~Boolean~
    }
    
    class AuthService {
        -UserRepository userRepository
        -PasswordEncoder passwordEncoder
        -JwtUtil jwtUtil
        +register(RegisterRequest) AuthResponse
        +login(LoginRequest) AuthResponse
        +validateToken(String) Boolean
    }
    
    class JwtUtil {
        -String secretKey
        -long expirationTime
        +generateToken(User) String
        +validateToken(String) Boolean
        +extractClaims(String) Claims
    }
    
    class UserRepository {
        <<interface>>
        +findByEmail(String) Optional~User~
        +existsByEmail(String) Boolean
    }
    
    AuthController --> AuthService
    AuthService --> UserRepository
    AuthService --> JwtUtil
    UserRepository --> User
```

### 7.2 Order Service Domain Model

```mermaid
classDiagram
    class Order {
        -Long id
        -Long userId
        -String userEmail
        -List~OrderItem~ items
        -BigDecimal totalAmount
        -OrderStatus status
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +addItem(OrderItem) void
        +calculateTotal() BigDecimal
    }
    
    class OrderItem {
        -Long id
        -Order order
        -String productId
        -String productName
        -BigDecimal price
        -Integer quantity
        -BigDecimal subtotal
        +calculateSubtotal() BigDecimal
    }
    
    class OrderStatus {
        <<enumeration>>
        PENDING
        CONFIRMED
        SHIPPED
        DELIVERED
        CANCELLED
    }
    
    class OrderController {
        -OrderService orderService
        +createOrder(OrderRequest, Long, String) ResponseEntity~Order~
        +getOrder(Long) ResponseEntity~Order~
        +getUserOrders(Long) ResponseEntity~List~Order~~
    }
    
    class OrderService {
        -OrderRepository orderRepository
        -CatalogClient catalogClient
        -KafkaTemplate kafkaTemplate
        +createOrder(OrderRequest, Long, String) Order
        +getProductWithResilience(String) ProductResponse
    }
    
    class CatalogClient {
        <<interface>>
        +getProductById(String) ProductResponse
    }
    
    Order "1" *-- "*" OrderItem
    Order --> OrderStatus
    OrderController --> OrderService
    OrderService --> CatalogClient
    OrderService --> Order
```

### 7.3 Catalog Service Domain Model

```mermaid
classDiagram
    class Product {
        -String id
        -String name
        -String description
        -BigDecimal price
        -Integer stock
        -String category
        -String imageUrl
        -Boolean active
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +isAvailable() Boolean
        +decrementStock(Integer) void
    }
    
    class ProductController {
        -ProductService productService
        +getAllProducts() ResponseEntity~List~Product~~
        +getProductById(String) ResponseEntity~Product~
        +createProduct(ProductRequest) ResponseEntity~Product~
        +updateProduct(String, ProductRequest) ResponseEntity~Product~
        +deleteProduct(String) ResponseEntity~Void~
    }
    
    class ProductService {
        -ProductRepository productRepository
        -CacheManager cacheManager
        +getAllProducts() List~Product~
        +getProductById(String) Product
        +createProduct(ProductRequest) Product
        +updateProduct(String, ProductRequest) Product
        +deleteProduct(String) void
    }
    
    class ProductRepository {
        <<interface>>
        +findByCategory(String) List~Product~
        +findByActiveTrue() List~Product~
    }
    
    ProductController --> ProductService
    ProductService --> ProductRepository
    ProductRepository --> Product
```

---

## 8. Package Structure

### 8.1 Auth Service Package Hierarchy

```
com.ecommerce.auth
├── AuthServiceApplication.java          # Spring Boot main class
├── controller/
│   └── AuthController.java              # REST endpoints
├── service/
│   └── AuthService.java                 # Business logic
├── repository/
│   └── UserRepository.java              # JPA repository
├── entity/
│   └── User.java                        # JPA entity
├── dto/
│   ├── RegisterRequest.java             # Registration DTO
│   ├── LoginRequest.java                # Login DTO
│   └── AuthResponse.java                # Authentication response DTO
├── security/
│   └── JwtUtil.java                     # JWT utility class
├── config/
│   └── SecurityConfig.java              # Security configuration
└── exception/
    ├── UserAlreadyExistsException.java  # Custom exception
    └── InvalidCredentialsException.java # Custom exception
```

### 8.2 Catalog Service Package Hierarchy

```
com.ecommerce.catalog
├── CatalogServiceApplication.java       # Spring Boot main class
├── controller/
│   └── ProductController.java           # REST endpoints
├── service/
│   └── ProductService.java              # Business logic with caching
├── repository/
│   └── ProductRepository.java           # MongoDB repository
├── document/
│   └── Product.java                     # MongoDB document
├── dto/
│   ├── ProductRequest.java              # Product creation/update DTO
│   └── ProductResponse.java             # Product response DTO
├── config/
│   ├── MongoConfig.java                 # MongoDB configuration
│   └── CacheConfig.java                 # Redis cache configuration
└── exception/
    └── ProductNotFoundException.java    # Custom exception
```

### 8.3 Order Service Package Hierarchy

```
com.ecommerce.order
├── OrderServiceApplication.java         # Spring Boot main class
├── controller/
│   └── OrderController.java             # REST endpoints
├── service/
│   └── OrderService.java                # Business logic with resilience
├── repository/
│   ├── OrderRepository.java             # JPA repository
│   └── OrderItemRepository.java         # JPA repository
├── entity/
│   ├── Order.java                       # JPA entity
│   ├── OrderItem.java                   # JPA entity
│   └── OrderStatus.java                 # Enum
├── dto/
│   ├── OrderRequest.java                # Order creation DTO
│   ├── OrderResponse.java               # Order response DTO
│   ├── OrderItemRequest.java            # Order item DTO
│   └── ProductResponse.java             # Feign client response DTO
├── client/
│   └── CatalogClient.java               # Feign client interface
├── event/
│   └── OrderCreatedEvent.java           # Kafka event DTO
├── config/
│   ├── FeignConfig.java                 # Feign configuration
│   ├── KafkaProducerConfig.java         # Kafka producer configuration
│   └── ResilienceConfig.java            # Resilience4j configuration
└── exception/
    ├── OrderNotFoundException.java      # Custom exception
    └── ProductUnavailableException.java # Custom exception
```

### 8.4 Gateway Service Package Hierarchy

```
com.ecommerce.gateway
├── GatewayServiceApplication.java       # Spring Boot main class
├── filter/
│   └── JwtAuthenticationFilter.java     # JWT validation filter
├── config/
│   ├── RedisConfig.java                 # Redis rate limiter configuration
│   └── CorsConfig.java                  # CORS configuration
└── exception/
    └── UnauthorizedException.java       # Custom exception
```

---

## 9. Exception Handling

### 9.1 Global Exception Handler (Auth Service Example)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("User Already Exists")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.UNAUTHORIZED.value())
            .error("Invalid Credentials")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Invalid request parameters")
            .details(errors)
            .path(request.getRequestURI())
            .build();
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .path(request.getRequestURI())
            .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### 9.2 Error Response DTO

```java
@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> details;
}
```

### 9.3 Custom Exceptions

```java
// Auth Service
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("User with email " + email + " already exists");
    }
}

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}

// Catalog Service
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productId) {
        super("Product with ID " + productId + " not found");
    }
}

// Order Service
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long orderId) {
        super("Order with ID " + orderId + " not found");
    }
}

public class ProductUnavailableException extends RuntimeException {
    public ProductUnavailableException(String productId) {
        super("Product " + productId + " is unavailable or out of stock");
    }
}
```

### 9.4 Exception Handling Strategy

| Exception Type | HTTP Status | Handling |
|----------------|-------------|----------|
| UserAlreadyExistsException | 409 Conflict | Return error with email |
| InvalidCredentialsException | 401 Unauthorized | Return generic error (security) |
| ProductNotFoundException | 404 Not Found | Return error with product ID |
| OrderNotFoundException | 404 Not Found | Return error with order ID |
| MethodArgumentNotValidException | 400 Bad Request | Return field validation errors |
| DataIntegrityViolationException | 409 Conflict | Return constraint violation error |
| FeignException | 503 Service Unavailable | Circuit breaker fallback |
| Exception (generic) | 500 Internal Server Error | Log stack trace, return generic error |

---

## 10. Logging

### 10.1 Logging Configuration (logback-spring.xml)

```xml
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <!-- Console Appender -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId}/%X{spanId}] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- File Appender -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>100MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId}/%X{spanId}] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- JSON Appender (for ELK Stack) -->
    <appender name="JSON" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.json</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application-%d{yyyy-MM-dd}.%i.json</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeContext>true</includeContext>
            <includeMdc>true</includeMdc>
            <includeStructuredArguments>true</includeStructuredArguments>
        </encoder>
    </appender>
    
    <!-- Root Logger -->
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
        <appender-ref ref="JSON"/>
    </root>
    
    <!-- Service-specific Loggers -->
    <logger name="com.ecommerce" level="DEBUG"/>
    <logger name="org.springframework.web" level="INFO"/>
    <logger name="org.springframework.security" level="DEBUG"/>
    <logger name="io.github.resilience4j" level="DEBUG"/>
    <logger name="org.springframework.cloud.gateway" level="DEBUG"/>
</configuration>
```

### 10.2 Structured Logging Example

```java
@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    
    public Order createOrder(OrderRequest request, Long userId, String userEmail) {
        log.info("Creating order for user: userId={}, email={}, itemCount={}", 
            userId, userEmail, request.getItems().size());
        
        try {
            Order order = new Order();
            order.setUserId(userId);
            order.setUserEmail(userEmail);
            
            for (OrderItemRequest itemRequest : request.getItems()) {
                log.debug("Fetching product: productId={}", itemRequest.getProductId());
                ProductResponse product = getProductWithResilience(itemRequest.getProductId());
                
                OrderItem item = new OrderItem();
                item.setProductId(product.getId());
                item.setProductName(product.getName());
                item.setPrice(product.getPrice());
                item.setQuantity(itemRequest.getQuantity());
                item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
                order.addItem(item);
            }
            
            order.setTotalAmount(order.calculateTotal());
            Order savedOrder = orderRepository.save(order);
            
            log.info("Order created successfully: orderId={}, totalAmount={}", 
                savedOrder.getId(), savedOrder.getTotalAmount());
            
            // Publish Kafka event
            kafkaTemplate.send("order-created", new OrderCreatedEvent(savedOrder));
            log.debug("OrderCreatedEvent published: orderId={}", savedOrder.getId());
            
            return savedOrder;
        } catch (Exception e) {
            log.error("Failed to create order: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }
}
```

### 10.3 Logging Levels

| Level | Usage | Examples |
|-------|-------|----------|
| ERROR | System errors, exceptions | Database connection failures, unhandled exceptions |
| WARN | Potential issues, degraded state | Circuit breaker open, cache miss rate high |
| INFO | Business events, lifecycle | Order created, user registered, service started |
| DEBUG | Detailed flow, troubleshooting | JWT validation, cache operations, Feign calls |
| TRACE | Very detailed, performance analysis | SQL queries, Redis commands |

### 10.4 Distributed Tracing Integration

```yaml
# application.yml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% sampling (dev), 0.1 (10% production)
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans

logging:
  pattern:
    level: '%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]'
```

**Trace Context Propagation**:

- `traceId`: Unique identifier for the entire request flow across services
- `spanId`: Unique identifier for the current operation within the trace
- Automatically propagated via HTTP headers (B3 format)
- Logged in every log statement for correlation

---

## 11. Security Design

### 4.1 Terraform Module Structure

```
terraform/
├── main.tf (root module)
├── variables.tf
├── outputs.tf
└── modules/
    ├── vpc/
    │   ├── vpc.tf
    │   ├── vpc-variables.tf
    │   └── vpc-outputs.tf
    ├── security-groups/
    │   ├── security-groups.tf
    │   ├── sg-variables.tf
    │   └── sg-outputs.tf
    ├── rds/
    │   ├── rds.tf
    │   ├── rds-variables.tf
    │   └── rds-outputs.tf
    ├── msk/
    │   ├── msk.tf
    │   ├── msk-variables.tf
    │   └── msk-outputs.tf
    └── ecs/
        ├── ecs.tf
        ├── ecs-variables.tf
        └── ecs-outputs.tf
```

### 4.2 Docker Multi-Stage Build

```dockerfile
# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 11.1 Authentication & Authorization

#### JWT Token Structure

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "123",           // User ID
    "email": "user@example.com",
    "role": "USER",
    "iat": 1722672000,      // Issued at
    "exp": 1722758400       // Expiration (24 hours)
  },
  "signature": "HMACSHA256(base64UrlEncode(header) + '.' + base64UrlEncode(payload), secret)"
}
```

#### JWT Configuration

```yaml
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-change-in-production}
  expiration: 86400000  # 24 hours in milliseconds
  issuer: ecommerce-platform
```

#### Password Security

- **Algorithm**: BCrypt with strength 12
- **Salt**: Auto-generated per password
- **Hashing**: One-way, irreversible
- **Validation**: Constant-time comparison

```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

### 11.2 Network Security

#### VPC Architecture

```
VPC (10.0.0.0/16)
├── Public Subnets (10.0.1.0/24, 10.0.2.0/24)
│   ├── Application Load Balancer
│   └── NAT Gateways
└── Private Subnets (10.0.11.0/24, 10.0.12.0/24)
    ├── ECS Tasks (Microservices)
    ├── RDS Instances (PostgreSQL)
    └── MSK Cluster (Kafka)
```

#### Security Groups

| Resource | Inbound Rules | Outbound Rules |
|----------|---------------|----------------|
| ALB | 80 (HTTP), 443 (HTTPS) from 0.0.0.0/0 | All traffic to ECS tasks |
| ECS Tasks | 8080-8083 from ALB | All traffic |
| RDS | 5432 from ECS tasks | None |
| MSK | 9092, 9094 from ECS tasks | None |
| Redis | 6379 from ECS tasks | None |

### 11.3 API Security

#### Rate Limiting (Redis Token Bucket)

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10    # Tokens per second
                redis-rate-limiter.burstCapacity: 20    # Max burst
                redis-rate-limiter.requestedTokens: 1   # Tokens per request
```

**Rate Limits by Service**:

- Auth Service: 10 requests/second (burst 20)
- Catalog Service: 50 requests/second (burst 100)
- Order Service: 20 requests/second (burst 40)

#### CORS Configuration

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://example.com"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
```

### 11.4 Data Security

#### Encryption at Rest

- **RDS**: AES-256 encryption enabled
- **EBS Volumes**: Encrypted with AWS KMS
- **S3 Buckets**: Server-side encryption (SSE-S3)

#### Encryption in Transit

- **HTTPS**: TLS 1.2+ for all external communication
- **Kafka**: TLS encryption for broker communication
- **RDS**: SSL/TLS connections enforced

#### Sensitive Data Handling

```yaml
# Spring Cloud Config encryption
encrypt:
  key: ${ENCRYPT_KEY:change-me-in-production}

# Encrypted properties in Git
spring:
  datasource:
    password: '{cipher}AQA...encrypted-value...'
```

### 11.5 Security Headers

```java
// Gateway Security Headers Filter
public class SecurityHeadersFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getResponse().getHeaders().add("X-Content-Type-Options", "nosniff");
        exchange.getResponse().getHeaders().add("X-Frame-Options", "DENY");
        exchange.getResponse().getHeaders().add("X-XSS-Protection", "1; mode=block");
        exchange.getResponse().getHeaders().add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        exchange.getResponse().getHeaders().add("Content-Security-Policy", "default-src 'self'");
        return chain.filter(exchange);
    }
}
```

---

## 12. Redis Cache Design

### 12.1 Cache Architecture

```mermaid
graph LR
    Client[Client Request]
    Gateway[API Gateway]
    Catalog[Catalog Service]
    Redis[(Redis Cache)]
    MongoDB[(MongoDB)]
    
    Client --> Gateway
    Gateway --> Catalog
    Catalog --> Redis
    Redis -->|Cache Miss| MongoDB
    MongoDB -->|Store in Cache| Redis
    Redis -->|Cache Hit| Catalog
```

### 12.2 Cache Configuration

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

### 12.3 Cache Strategy (Cache-Aside Pattern)

#### Read Flow

1. Application checks cache for data
2. **Cache Hit**: Return cached data (< 50ms)
3. **Cache Miss**: Query database → Store in cache → Return data (~200-300ms)

#### Write Flow

1. Application updates database
2. Invalidate/evict cache entry
3. Next read will repopulate cache

#### Implementation

```java
@Service
public class ProductService {
    
    @Cacheable(value = "products", key = "'all'")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    @Cacheable(value = "products", key = "#id")
    public Product getProductById(String id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }
    
    @Cacheable(value = "products", key = "'category:' + #category")
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }
    
    @CacheEvict(value = "products", allEntries = true)
    public Product createProduct(ProductRequest request) {
        Product product = new Product();
        // ... set fields
        return productRepository.save(product);
    }
    
    @CacheEvict(value = "products", allEntries = true)
    public Product updateProduct(String id, ProductRequest request) {
        Product product = getProductById(id);
        // ... update fields
        return productRepository.save(product);
    }
    
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }
}
```

### 12.4 Cache Keys

| Cache Key | Data | TTL |
|-----------|------|-----|
| `products:all` | All products list | 10 minutes |
| `products:{id}` | Single product by ID | 10 minutes |
| `products:category:{name}` | Products by category | 10 minutes |
| `ratelimiter:{userId}` | Rate limit token bucket | 1 second |

### 12.5 Cache Metrics

```yaml
# Exposed Prometheus metrics
cache_gets_total{cache="products",result="hit"}
cache_gets_total{cache="products",result="miss"}
cache_puts_total{cache="products"}
cache_evictions_total{cache="products"}
```

**Performance Impact**:

- Cache Hit Rate: 80-90% (typical)
- Cache Hit Response Time: < 50ms
- Cache Miss Response Time: 200-300ms
- Database Load Reduction: 80-90%

---

## 13. Kafka Design

### 13.1 Kafka Architecture

```mermaid
graph TB
    Order[Order Service]
    Kafka[Apache Kafka Cluster]
    Topic[order-created Topic]
    Consumer1[Notification Service]
    Consumer2[Analytics Service]
    Consumer3[Inventory Service]
    
    Order -->|Publish OrderCreatedEvent| Kafka
    Kafka --> Topic
    Topic --> Consumer1
    Topic --> Consumer2
    Topic --> Consumer3
```

### 13.2 Kafka Configuration

#### Producer Configuration (Order Service)

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092  # MSK brokers in production
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: 1                          # Leader acknowledgment
      retries: 3
      properties:
        max.in.flight.requests.per.connection: 5
        enable.idempotence: true
```

#### Producer Implementation

```java
@Service
public class OrderService {
    
    @Autowired
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    
    private static final String TOPIC = "order-created";
    
    public Order createOrder(OrderRequest request, Long userId, String userEmail) {
        // ... create order logic
        Order savedOrder = orderRepository.save(order);
        
        // Publish event
        OrderCreatedEvent event = OrderCreatedEvent.builder()
            .orderId(savedOrder.getId())
            .userId(savedOrder.getUserId())
            .userEmail(savedOrder.getUserEmail())
            .totalAmount(savedOrder.getTotalAmount())
            .createdAt(savedOrder.getCreatedAt())
            .build();
        
        kafkaTemplate.send(TOPIC, String.valueOf(event.getOrderId()), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("OrderCreatedEvent published: orderId={}, partition={}, offset={}",
                        event.getOrderId(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish OrderCreatedEvent: orderId={}, error={}",
                        event.getOrderId(), ex.getMessage());
                }
            });
        
        return savedOrder;
    }
}
```

### 13.3 Event Schema

#### OrderCreatedEvent

```java
@Data
@Builder
public class OrderCreatedEvent {
    private Long orderId;
    private Long userId;
    private String userEmail;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
```

**JSON Payload**:

```json
{
  "orderId": 123,
  "userId": 456,
  "userEmail": "user@example.com",
  "totalAmount": 1999.98,
  "createdAt": "2026-08-04T10:30:00Z"
}
```

### 13.4 Topic Configuration

#### MSK Cluster (AWS)

```yaml
Cluster Configuration:
- Kafka Version: 3.5.1
- Brokers: 2 (kafka.t3.small)
- Availability Zones: 2
- Storage: 100GB EBS per broker
- Encryption: TLS (in-transit and at-rest)
- Auto-create Topics: Enabled
```

#### Topic: order-created

```yaml
Topic Configuration:
- Partitions: 3
- Replication Factor: 2
- Retention: 7 days (168 hours)
- Compression: snappy
- Min In-Sync Replicas: 1
```

### 13.5 Consumer Groups (Future Implementation)

| Consumer Group | Service | Purpose |
|----------------|---------|----------|
| notification-service-group | Notification Service | Send order confirmation emails/SMS |
| analytics-service-group | Analytics Service | Track order metrics and trends |
| inventory-service-group | Inventory Service | Update stock levels |

### 13.6 Event-Driven Patterns

#### Fire-and-Forget (Current)

- Order Service publishes event after order creation
- No waiting for consumer acknowledgment
- Fast response to client
- Eventual consistency

#### Future Enhancements

- **Outbox Pattern**: Transactional event publishing
- **Saga Pattern**: Distributed transactions across services
- **Event Sourcing**: Store events as source of truth
- **CQRS**: Separate read/write models

---

## 14. Configuration

### 14.1 Spring Cloud Config Server

#### Git Repository Structure

```
config-repo/
├── application.yml              # Shared configuration
├── application-dev.yml          # Development environment
├── application-prod.yml         # Production environment
├── gateway-service.yml          # Gateway-specific config
├── gateway-service-dev.yml
├── gateway-service-prod.yml
├── auth-service.yml
├── auth-service-dev.yml
├── auth-service-prod.yml
├── catalog-service.yml
├── catalog-service-dev.yml
├── catalog-service-prod.yml
├── order-service.yml
├── order-service-dev.yml
└── order-service-prod.yml
```

#### Config Server Configuration

```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-org/config-repo
          default-label: main
          search-paths: '{application}'
          clone-on-start: true
          force-pull: true
  security:
    user:
      name: admin
      password: ${CONFIG_SERVER_PASSWORD:admin123}
```

#### Client Configuration (Auth Service Example)

```yaml
spring:
  application:
    name: auth-service
  profiles:
    active: dev
  config:
    import: optional:configserver:http://localhost:8888
  cloud:
    config:
      username: admin
      password: admin123
      fail-fast: true
      retry:
        max-attempts: 6
        initial-interval: 1000
        multiplier: 1.1
```

### 14.2 Environment-Specific Configuration

#### Development (application-dev.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_db
    username: postgres
    password: postgres
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update

logging:
  level:
    com.ecommerce: DEBUG

management:
  tracing:
    sampling:
      probability: 1.0  # 100% sampling
```

#### Production (application-prod.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${RDS_ENDPOINT}:5432/auth_db
    username: ${RDS_USERNAME}
    password: ${RDS_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate

logging:
  level:
    com.ecommerce: INFO

management:
  tracing:
    sampling:
      probability: 0.1  # 10% sampling
```

### 14.3 Configuration Refresh

#### Actuator Refresh Endpoint

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,refresh
```

**Refresh Configuration**:

```bash
# Trigger configuration refresh
curl -X POST http://localhost:8081/actuator/refresh
```

#### @RefreshScope Annotation

```java
@Service
@RefreshScope
public class DynamicConfigService {
    
    @Value("${feature.new-checkout-flow.enabled}")
    private boolean newCheckoutFlowEnabled;
    
    public boolean isNewCheckoutFlowEnabled() {
        return newCheckoutFlowEnabled;
    }
}
```

### 14.4 Secrets Management

#### Environment Variables (Production)

```bash
# Database credentials
export RDS_USERNAME=admin
export RDS_PASSWORD=secure-password

# JWT secret
export JWT_SECRET=your-256-bit-secret-key

# Config server password
export CONFIG_SERVER_PASSWORD=secure-config-password

# Kafka credentials
export KAFKA_SASL_USERNAME=kafka-user
export KAFKA_SASL_PASSWORD=kafka-password
```

#### AWS Secrets Manager Integration (Future)

```yaml
spring:
  cloud:
    aws:
      secretsmanager:
        enabled: true
        region: us-east-1
```

---

## 15. Deployment Design

### 15.1 Container Deployment (Docker)

#### Multi-Stage Dockerfile (All Services)

```dockerfile
# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app

# Copy POM and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# JVM optimization
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```

### 15.2 AWS ECS Fargate Deployment

#### ECS Task Definition (Gateway Service)

```json
{
  "family": "gateway-service",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "containerDefinitions": [
    {
      "name": "gateway-service",
      "image": "123456789012.dkr.ecr.us-east-1.amazonaws.com/gateway-service:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {"name": "SPRING_PROFILES_ACTIVE", "value": "prod"},
        {"name": "CONFIG_SERVER_URI", "value": "http://config-server:8888"}
      ],
      "secrets": [
        {"name": "JWT_SECRET", "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789012:secret:jwt-secret"}
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/gateway-service",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

#### ECS Service Configuration

```yaml
Service Configuration:
- Desired Count: 2
- Launch Type: FARGATE
- Platform Version: LATEST
- Subnets: Private subnets (10.0.11.0/24, 10.0.12.0/24)
- Security Groups: ECS tasks security group
- Load Balancer: Application Load Balancer
- Target Group: gateway-service-tg (port 8080)
- Health Check: /actuator/health (30s interval, 2 healthy threshold)
- Auto Scaling: Target tracking (CPU 70%)
```

### 15.3 Deployment Architecture

```mermaid
graph TB
    Internet[Internet]
    ALB[Application Load Balancer]
    
    subgraph "Public Subnets"
        NAT1[NAT Gateway AZ1]
        NAT2[NAT Gateway AZ2]
    end
    
    subgraph "Private Subnets - AZ1"
        Gateway1[Gateway Service]
        Auth1[Auth Service]
        Catalog1[Catalog Service]
        Order1[Order Service]
    end
    
    subgraph "Private Subnets - AZ2"
        Gateway2[Gateway Service]
        Auth2[Auth Service]
        Catalog2[Catalog Service]
        Order2[Order Service]
    end
    
    subgraph "Data Layer"
        RDS1[(Auth DB)]
        RDS2[(Order DB)]
        MongoDB[(Catalog DB)]
        Redis[(Redis)]
        Kafka[Kafka Cluster]
    end
    
    Internet --> ALB
    ALB --> Gateway1
    ALB --> Gateway2
    
    Gateway1 --> Auth1
    Gateway1 --> Catalog1
    Gateway1 --> Order1
    
    Gateway2 --> Auth2
    Gateway2 --> Catalog2
    Gateway2 --> Order2
    
    Auth1 --> RDS1
    Auth2 --> RDS1
    Order1 --> RDS2
    Order2 --> RDS2
    Catalog1 --> MongoDB
    Catalog2 --> MongoDB
    
    Gateway1 --> Redis
    Gateway2 --> Redis
    Catalog1 --> Redis
    Catalog2 --> Redis
    
    Order1 --> Kafka
    Order2 --> Kafka
```

### 15.4 CI/CD Pipeline (GitHub Actions Example)

```yaml
name: Deploy to ECS

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v2
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: us-east-1
      
      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v1
      
      - name: Build and push Docker image
        env:
          ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
          ECR_REPOSITORY: gateway-service
          IMAGE_TAG: ${{ github.sha }}
        run: |
          docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
      
      - name: Update ECS service
        run: |
          aws ecs update-service \
            --cluster ecommerce-cluster \
            --service gateway-service \
            --force-new-deployment
```

### 15.5 Local Development (Docker Compose)

```yaml
version: '3.8'

services:
  postgres-auth:
    image: postgres:15.4-alpine
    environment:
      POSTGRES_DB: auth_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres-auth-data:/var/lib/postgresql/data
  
  postgres-order:
    image: postgres:15.4-alpine
    environment:
      POSTGRES_DB: order_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5433:5432"
    volumes:
      - postgres-order-data:/var/lib/postgresql/data
  
  mongodb:
    image: mongo:7.0
    ports:
      - "27017:27017"
    volumes:
      - mongodb-data:/data/db
  
  redis:
    image: redis:7.2-alpine
    ports:
      - "6379:6379"
  
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
  
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
  
  prometheus:
    image: prom/prometheus:v2.47.0
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
  
  grafana:
    image: grafana/grafana:10.2.0
    ports:
      - "3000:3000"
    volumes:
      - ./monitoring/grafana/provisioning:/etc/grafana/provisioning
      - grafana-data:/var/lib/grafana

volumes:
  postgres-auth-data:
  postgres-order-data:
  mongodb-data:
  grafana-data:
```

### 15.6 Deployment Checklist

#### Pre-Deployment

- [ ] Run unit tests (`mvn test`)
- [ ] Run integration tests
- [ ] Build Docker images
- [ ] Push images to ECR
- [ ] Update environment variables in ECS task definitions
- [ ] Verify database migrations
- [ ] Check Config Server connectivity

#### Deployment

- [ ] Deploy Config Server first
- [ ] Deploy Auth Service
- [ ] Deploy Catalog Service
- [ ] Deploy Order Service
- [ ] Deploy Gateway Service last
- [ ] Verify health checks (`/actuator/health`)
- [ ] Check ALB target group health

#### Post-Deployment

- [ ] Smoke test critical endpoints
- [ ] Verify Prometheus metrics scraping
- [ ] Check Grafana dashboards
- [ ] Monitor CloudWatch logs
- [ ] Test distributed tracing
- [ ] Verify Kafka event publishing
- [ ] Check Redis cache hit rates

---

**Document Version**: 2.0  
**Last Updated**: August 04, 2026  
**Author**: E-Commerce Platform Team  
**Total Sections**: 15  
**Total Pages**: ~50 (estimated)