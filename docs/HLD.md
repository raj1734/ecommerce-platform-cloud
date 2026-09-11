# High-Level Design (HLD) - E-Commerce Microservices Platform

## 1. System Overview

The E-Commerce Microservices Platform is a distributed, cloud-native application built using microservices architecture.
It implements core e-commerce functionalities including user authentication, product catalog management, shopping cart,
and order processing.


## 3. Architecture Pattern

### 3.1 Microservices Architecture

- **Pattern**: Database-per-Service
- **Communication**: Synchronous (REST/HTTP) and Asynchronous (Kafka)
- **API Gateway**: Spring Cloud Gateway for centralized routing
- **Configuration**: Externalized using Spring Cloud Config Server

### 3.2 Service Boundaries

#### Gateway Service (Port: 8080)
- **Responsibility**: API Gateway, routing, rate limiting, JWT validation
- **Technology**: Spring Cloud Gateway, Redis
- **Key Features**:
  - Centralized routing to all microservices
  - JWT token validation
  - Rate limiting using Redis
  - CORS configuration
  - Request/Response logging

#### Auth Service (Port: 8081)
- **Responsibility**: User authentication and authorization
- **Database**: PostgreSQL (auth_db)
- **Key Features**:
  - User registration
  - Login with JWT token generation
  - Token validation
  - Password encryption (BCrypt)

#### Catalog Service (Port: 8082)
- **Responsibility**: Product catalog management
- **Database**: MongoDB (catalog_db)
- **Caching**: Redis (Cache-Aside Pattern)
- **Key Features**:
  - CRUD operations for products
  - Category-based filtering
  - Product search
  - Redis caching for performance

#### Order Service (Port: 8083)
- **Responsibility**: Order management and processing
- **Database**: PostgreSQL (order_db)
- **Integration**: Feign Client to Catalog Service
- **Event Publishing**: Kafka (OrderCreated events)
- **Key Features**:
  - Order creation with product validation
  - Circuit Breaker (Resilience4j) for catalog service calls
  - Retry mechanism
  - Kafka event publishing

#### Config Server (Port: 8888)
- **Responsibility**: Centralized configuration management
- **Backend**: Git repository
- **Security**: Basic authentication

## 3. Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Cloud**: Spring Cloud 2023.0.0
- **Java Version**: 17
- **Build Tool**: Maven

### Databases
- **PostgreSQL 15.4**: Auth Service, Order Service
- **MongoDB 7.0**: Catalog Service
- **Redis 7.2**: Caching, Rate Limiting

### Messaging
- **Apache Kafka 3.5.1**: Event-driven communication
- **Zookeeper**: Kafka coordination

### Infrastructure
- **Container**: Docker
- **Orchestration**: AWS ECS Fargate
- **IaC**: Terraform
- **Load Balancer**: AWS Application Load Balancer

### Observability
- **Metrics**: Micrometer + Prometheus
- **Visualization**: Grafana
- **Tracing**: Micrometer Tracing (Brave)
- **Health Checks**: Spring Boot Actuator

## 4. Communication Patterns

### 4.1 Synchronous Communication
- **Gateway → Services**: HTTP/REST via Spring Cloud Gateway
- **Order Service → Catalog Service**: Spring Cloud OpenFeign
- **Resilience**: Circuit Breaker, Retry (Resilience4j)

### 4.2 Asynchronous Communication
- **Order Service → Kafka**: OrderCreated event publishing
- **Topic**: `order-created`
- **Pattern**: Fire-and-forget (no Saga/Outbox for Week 1)

## 5. Security

### 5.1 Authentication & Authorization
- **JWT Tokens**: JJWT library (0.12.3)
- **Token Validation**: Gateway filter
- **User Context Propagation**: Custom headers (X-User-Id, X-User-Email)

### 5.2 Network Security
- **VPC**: Public/Private subnet architecture
- **Security Groups**: Layered (ALB, ECS, RDS, MSK)
- **Database**: Private subnet only
- **Encryption**: TLS for Kafka, encrypted RDS storage

## 6. Data Management

### 6.1 Database-per-Service Pattern
- **Auth DB**: User credentials, roles
- **Order DB**: Orders, order items
- **Catalog DB**: Products, categories

### 6.2 Caching Strategy
- **Pattern**: Cache-Aside
- **TTL**: 10 minutes
- **Cache Keys**: Product ID, category, "all" products
- **Eviction**: On create/update/delete operations

## 7. Deployment Architecture

### 7.1 AWS Infrastructure
- **VPC**: Multi-AZ with NAT gateways
- **Compute**: ECS Fargate (serverless containers)
- **Database**: RDS PostgreSQL (Multi-AZ capable)
- **Messaging**: Amazon MSK (Managed Kafka)
- **Load Balancing**: Application Load Balancer

### 7.2 Container Strategy
- **Multi-stage Dockerfiles**: Build + Runtime stages
- **Base Images**: Eclipse Temurin JRE Alpine
- **Registry**: Amazon ECR (Elastic Container Registry)

## 8. Monitoring & Observability

### 8.1 Metrics Collection
- **Prometheus**: Scrapes `/actuator/prometheus` endpoints
- **Metrics**: HTTP requests, JVM stats, custom business metrics
- **Retention**: 7 days (CloudWatch Logs)

### 8.2 Distributed Tracing
- **Correlation ID**: Propagated across all services
- **Headers**: X-B3-TraceId, X-B3-SpanId
- **Sampling**: 100% (development), configurable for production

### 8.3 Health Checks
- **Endpoint**: `/actuator/health`
- **Checks**: Database connectivity, circuit breaker status
- **ALB Health Check**: 30s interval, 2 healthy threshold

## 9. Scalability & Resilience

### 9.1 Horizontal Scaling
- **ECS Auto Scaling**: CPU/Memory based
- **Stateless Services**: All services are stateless
- **Session Management**: JWT (no server-side sessions)

### 9.2 Fault Tolerance
- **Circuit Breaker**: Catalog service calls from Order service
- **Retry**: Exponential backoff (3 attempts)
- **Fallback**: Default product data on catalog failure
- **Rate Limiting**: Redis-backed token bucket

## 10. Configuration Management

### 10.1 Externalized Configuration
- **Config Server**: Git-backed configuration
- **Environment-specific**: dev, staging, prod profiles
- **Secrets**: Environment variables (DB passwords, JWT secrets)
- **Refresh**: Spring Cloud Bus (future enhancement)

## 11. API Design

### 11.1 RESTful Endpoints

#### Gateway Routes
- `POST /api/auth/register` → Auth Service
- `POST /api/auth/login` → Auth Service
- `GET /api/catalog/products` → Catalog Service
- `POST /api/orders` → Order Service

#### Response Format
```json
{
  "status": "success",
  "data": {},
  "message": "Operation completed"
}
```

### 11.2 Error Handling
- **HTTP Status Codes**: 200, 201, 400, 401, 404, 500
- **Error Response**:
```json
{
  "timestamp": "2026-08-03T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/orders"
}
```

## 12. Future Enhancements

### Week 2 Additions
- Cart Service implementation
- Advanced Saga pattern for distributed transactions
- Outbox pattern for reliable event publishing

### Week 3 Additions
- Full ECS deployment with task definitions
- CI/CD pipeline (GitHub Actions)
- Advanced Grafana dashboards
- OpenTelemetry integration

## 13. Non-Functional Requirements

### 13.1 Performance
- **Response Time**: < 200ms (p95) for cached requests
- **Throughput**: 1000 requests/second per service

### 13.2 Availability
- **Target**: 99.9% uptime
- **Multi-AZ**: Database and compute

### 13.3 Security
- **Authentication**: JWT with 24-hour expiration
- **Authorization**: Role-based access control
- **Data Encryption**: At rest and in transit

### 13.4 Maintainability
- **Code Quality**: SonarQube integration
- **Documentation**: OpenAPI/Swagger specs
- **Logging**: Structured JSON logs

---

**Document Version**: 1.0  
**Last Updated**: August 03, 2026  
**Author**: E-Commerce Platform Team