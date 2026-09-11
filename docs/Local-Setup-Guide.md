# Building and Running the E-Commerce Platform Locally

**Document Version:** 1.0  
**Last Updated:** January 2025  
**Project:** Distributed E-Commerce Microservices Platform  
**Purpose:** Local development setup and verification guide

---

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Build All Services](#build-all-services)
4. [Start Services in Order](#start-services-in-order)
5. [Testing Individual Services](#testing-individual-services)
6. [Verify Gateway Routing](#verify-gateway-routing)
7. [Docker Compose Alternative](#docker-compose-alternative)
8. [Troubleshooting](#troubleshooting)
9. [Service Ports Reference](#service-ports-reference)

---

## Overview

This guide walks through the process of building and running the E-Commerce Microservices Platform locally to fulfill the Week 1 requirement:

> **"API Gateway routing requests correctly to local/stubbed services."**

### Architecture Flow

```
Client Request → Gateway (8080) → Backend Services (8081-8083)
                      ↓
                Config Server (8888)
```

---

## Prerequisites

Before starting, ensure you have:

- ✅ **Java 17** installed (`java -version`)
- ✅ **Maven 3.8+** installed (`mvn -version`)
- ✅ **PostgreSQL** running (if not using Docker)
- ✅ **Ports available:** 8080, 8081, 8082, 8083, 8888
- ✅ **Git** for version control
- ✅ **Docker & Docker Compose** (optional, for containerized setup)

---

## Build All Services

From the workspace root directory, build all modules:

```powershell
# Build all services
mvn clean install
```

**What this does:**
- Compiles all modules defined in parent pom.xml
- Runs unit tests for each service
- Creates executable JAR files in each service's `target/` directory
- Installs artifacts to local Maven repository

**Expected Output:**
```
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for E-Commerce Microservices Platform 1.0.0-SNAPSHOT:
[INFO] 
[INFO] E-Commerce Microservices Platform .................. SUCCESS [  0.123 s]
[INFO] config-server ...................................... SUCCESS [  5.432 s]
[INFO] auth-service ....................................... SUCCESS [  8.765 s]
[INFO] catalog-service .................................... SUCCESS [  7.234 s]
[INFO] order-service ...................................... SUCCESS [  9.876 s]
[INFO] gateway-service .................................... SUCCESS [  6.543 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## Start Services in Order

### Critical: Service Startup Sequence

Services must be started in this specific order to ensure proper dependency resolution:

1. **Config Server** (provides configuration to all services)
2. **Backend Services** (Auth, Catalog, Order - can start in parallel)
3. **Gateway Service** (routes to backend services)

### Step 1: Start Config Server

```powershell
cd config-server
mvn spring-boot:run
```

**Wait for startup confirmation:**
```
Started ConfigServerApplication in 3.456 seconds (JVM running for 4.123)
```

**Verify Config Server:**
```powershell
curl http://localhost:8888/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

### Step 2: Start Backend Services (Parallel)

Open **three separate terminal windows** and run:

#### Terminal 1 - Auth Service

```powershell
cd auth-service
mvn spring-boot:run
```

**Startup confirmation:**
```
Started AuthServiceApplication in 5.678 seconds (JVM running for 6.789)
```

#### Terminal 2 - Catalog Service

```powershell
cd catalog-service
mvn spring-boot:run
```

**Startup confirmation:**
```
Started CatalogServiceApplication in 4.567 seconds (JVM running for 5.678)
```

#### Terminal 3 - Order Service

```powershell
cd order-service
mvn spring-boot:run
```

**Startup confirmation:**
```
Started OrderServiceApplication in 6.789 seconds (JVM running for 7.890)
```

### Step 3: Start Gateway Service (Last)

Once all backend services are running:

```powershell
cd gateway-service
mvn spring-boot:run
```

**Startup confirmation:**
```
Started GatewayServiceApplication in 4.321 seconds (JVM running for 5.432)
```

---

## Testing Individual Services

After starting all services, it's important to test each service individually (directly on their ports) before testing
through the Gateway. This helps isolate issues and verify that each microservice is functioning correctly.

### Why Test Individual Services?

- ✅ **Isolate Issues:** Determine if problems are in the service itself or in Gateway routing
- ✅ **Verify Configuration:** Ensure each service loaded correct configuration from Config Server
- ✅ **Database Connectivity:** Confirm database connections are working
- ✅ **Service Logic:** Test business logic before adding Gateway complexity
- ✅ **Debugging:** Easier to debug when testing services directly

### Prerequisites for Testing

**Note:** Config Server uses Basic Authentication. Include credentials in all curl commands:

- **Username:** `admin`
- **Password:** `admin123`

---

### Test 1: Config Server (Port 8888)

#### Health Check

```powershell
curl http://admin:admin123@localhost:8888/actuator/health
```

**Expected Response:**

```json
{
  "status": "UP"
}
```

#### Verify Configuration Loading

Test that Config Server is serving configurations from the local `config-repo/` directory:

```powershell
# Test common configuration
curl http://admin:admin123@localhost:8888/application/default

# Test auth-service configuration (dev profile)
curl http://admin:admin123@localhost:8888/auth-service/dev

# Test catalog-service configuration
curl http://admin:admin123@localhost:8888/catalog-service/default

# Test order-service configuration
curl http://admin:admin123@localhost:8888/order-service/default

# Test gateway-service configuration
curl http://admin:admin123@localhost:8888/gateway-service/default
```

**Expected Response Format:**

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
        "server.port": 8081,
        "spring.application.name": "auth-service",
        "jwt.expiration": 86400000
      }
    },
    {
      "name": "file:./config-repo/application-dev.yml",
      "source": { ... }
    },
    {
      "name": "file:./config-repo/application.yml",
      "source": { ... }
    }
  ]
}
```

**✅ Success Criteria:**

- Status is "UP"
- Configuration files are loaded from `file:./config-repo/`
- All three property sources appear (service-specific, profile-specific, common)
- Port numbers match expected values (8081, 8082, 8083)

---

### Test 2: Auth Service (Port 8081)

#### Health Check

```powershell
curl http://localhost:8081/actuator/health
```

**Expected Response:**

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

#### Test User Registration

Register a new user directly on the Auth Service:

```powershell
curl -X POST http://localhost:8081/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "email": "testuser@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Expected Response (201 Created):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlckBleGFtcGxlLmNvbSIsImlhdCI6MTY0MDk5NTIwMCwiZXhwIjoxNjQxMDgxNjAwfQ.signature",
  "email": "testuser@example.com",
  "role": "USER"
}
```

**Save the JWT token for subsequent tests.**

#### Test User Login

```powershell
curl -X POST http://localhost:8081/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    "email": "testuser@example.com",
    "password": "password123"
  }'
```

**Expected Response (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "testuser@example.com",
  "role": "USER"
}
```

#### Test Invalid Login

```powershell
curl -X POST http://localhost:8081/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    "email": "testuser@example.com",
    "password": "wrongpassword"
  }'
```

**Expected Response (401 Unauthorized):**

```json
{
  "error": "Invalid credentials"
}
```

**✅ Success Criteria:**

- Health check shows database connection is UP
- User registration returns JWT token
- User login with correct credentials returns token
- Invalid credentials return 401 error
- JWT token format is valid (three base64-encoded parts separated by dots)

---

### Test 3: Catalog Service (Port 8082)

#### Health Check

```powershell
curl http://localhost:8082/actuator/health
```

**Expected Response:**

```json
{
  "status": "UP",
  "components": {
    "mongo": {
      "status": "UP",
      "details": {
        "version": "5.0.x"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "6.2.x"
      }
    },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

#### Test Get All Products

```powershell
curl http://localhost:8082/api/products `
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**

```json
[
  {
    "id": "507f1f77bcf86cd799439011",
    "name": "Wireless Mouse",
    "description": "Ergonomic wireless mouse with USB receiver",
    "price": 29.99,
    "stock": 150,
    "category": "Electronics",
    "active": true,
    "createdAt": "2025-01-01T10:00:00Z",
    "updatedAt": "2025-01-15T14:30:00Z"
  },
  {
    "id": "507f1f77bcf86cd799439012",
    "name": "Mechanical Keyboard",
    "description": "RGB mechanical gaming keyboard",
    "price": 89.99,
    "stock": 75,
    "category": "Electronics",
    "active": true,
    "createdAt": "2025-01-01T10:00:00Z",
    "updatedAt": "2025-01-15T14:30:00Z"
  }
]
```

#### Test Get Product by ID

```powershell
curl http://localhost:8082/api/products/507f1f77bcf86cd799439011 `
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**

```json
{
  "id": "507f1f77bcf86cd799439011",
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse with USB receiver",
  "price": 29.99,
  "stock": 150,
  "category": "Electronics",
  "active": true
}
```

#### Test Search Products by Category

```powershell
curl "http://localhost:8082/api/products?category=Electronics" `
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Test Create Product (Admin Only)

```powershell
curl -X POST http://localhost:8082/api/products `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" `
  -d '{
    "name": "USB-C Cable",
    "description": "High-speed USB-C charging cable",
    "price": 12.99,
    "stock": 200,
    "category": "Accessories"
  }'
```

**Expected Response (201 Created):**

```json
{
  "id": "507f1f77bcf86cd799439013",
  "name": "USB-C Cable",
  "description": "High-speed USB-C charging cable",
  "price": 12.99,
  "stock": 200,
  "category": "Accessories",
  "active": true,
  "createdAt": "2025-01-15T15:00:00Z",
  "updatedAt": "2025-01-15T15:00:00Z"
}
```

**✅ Success Criteria:**

- Health check shows MongoDB and Redis connections are UP
- GET requests return product list
- Product search by category works
- Product creation returns new product with generated ID
- Unauthorized requests (without JWT) return 401

---

### Test 4: Order Service (Port 8083)

#### Health Check

```powershell
curl http://localhost:8083/actuator/health
```

**Expected Response:**

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL"
      }
    },
    "circuitBreakers": {
      "status": "UP",
      "details": {
        "catalogService": "CLOSED"
      }
    },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

#### Test Create Order

```powershell
curl -X POST http://localhost:8083/api/orders `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_JWT_TOKEN" `
  -d '{
    "items": [
      {
        "productId": "507f1f77bcf86cd799439011",
        "quantity": 2
      },
      {
        "productId": "507f1f77bcf86cd799439012",
        "quantity": 1
      }
    ]
  }'
```

**Expected Response (201 Created):**

```json
{
  "id": 1,
  "userId": 123,
  "userEmail": "testuser@example.com",
  "items": [
    {
      "id": 1,
      "productId": "507f1f77bcf86cd799439011",
      "productName": "Wireless Mouse",
      "price": 29.99,
      "quantity": 2,
      "subtotal": 59.98
    },
    {
      "id": 2,
      "productId": "507f1f77bcf86cd799439012",
      "productName": "Mechanical Keyboard",
      "price": 89.99,
      "quantity": 1,
      "subtotal": 89.99
    }
  ],
  "totalAmount": 149.97,
  "status": "PENDING",
  "createdAt": "2025-01-15T15:30:00Z",
  "updatedAt": "2025-01-15T15:30:00Z"
}
```

#### Test Get User Orders

```powershell
curl http://localhost:8083/api/orders `
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**

```json
[
  {
    "id": 1,
    "userId": 123,
    "userEmail": "testuser@example.com",
    "totalAmount": 149.97,
    "status": "PENDING",
    "createdAt": "2025-01-15T15:30:00Z"
  }
]
```

#### Test Get Order by ID

```powershell
curl http://localhost:8083/api/orders/1 `
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**

```json
{
  "id": 1,
  "userId": 123,
  "userEmail": "testuser@example.com",
  "items": [ ... ],
  "totalAmount": 149.97,
  "status": "PENDING",
  "createdAt": "2025-01-15T15:30:00Z",
  "updatedAt": "2025-01-15T15:30:00Z"
}
```

#### Test Update Order Status (Admin Only)

```powershell
curl -X PATCH http://localhost:8083/api/orders/1/status `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" `
  -d '{
    "status": "CONFIRMED"
  }'
```

**Expected Response (200 OK):**

```json
{
  "id": 1,
  "status": "CONFIRMED",
  "updatedAt": "2025-01-15T15:45:00Z"
}
```

**✅ Success Criteria:**

- Health check shows database and circuit breaker status
- Order creation calculates totals correctly
- Order creation calls Catalog Service to fetch product details
- User can retrieve their own orders
- Order status updates work for admin users
- Circuit breaker state is CLOSED (healthy)

---

### Test 5: Gateway Service (Port 8080)

#### Health Check

```powershell
curl http://localhost:8080/actuator/health
```

**Expected Response:**

```json
{
  "status": "UP",
  "components": {
    "redis": {
      "status": "UP",
      "details": {
        "version": "6.2.x"
      }
    },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

#### Test Gateway Routes

Verify that Gateway can reach all backend services:

```powershell
# Test Auth Service route
curl http://localhost:8080/api/auth/actuator/health

# Test Catalog Service route
curl http://localhost:8080/api/catalog/actuator/health

# Test Order Service route
curl http://localhost:8080/api/orders/actuator/health
```

**Expected Response for Each:**

```json
{
  "status": "UP"
}
```

#### Test Rate Limiting

Send multiple requests rapidly to test Redis-based rate limiting:

```powershell
# PowerShell script to test rate limiting
for ($i = 1; $i -le 25; $i++) {
    Write-Host "Request $i"
    curl http://localhost:8080/api/catalog/api/products `
      -H "Authorization: Bearer YOUR_JWT_TOKEN"
    Start-Sleep -Milliseconds 100
}
```

**Expected Behavior:**

- First 20 requests succeed (burst capacity)
- Requests 21-25 may return 429 (Too Many Requests) if rate limit exceeded

**Expected Response (429 Too Many Requests):**

```json
{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Try again later."
}
```

#### Test CORS Headers

```powershell
curl -X OPTIONS http://localhost:8080/api/catalog/api/products `
  -H "Origin: http://localhost:3000" `
  -H "Access-Control-Request-Method: GET" `
  -v
```

**Expected Headers in Response:**

```
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Max-Age: 3600
```

**✅ Success Criteria:**

- Health check shows Redis connection is UP
- All backend service routes are accessible through Gateway
- Rate limiting works (429 errors after burst capacity)
- CORS headers are present in OPTIONS responses
- Gateway strips path prefixes correctly (`/api/auth/**` → `/api/**`)

---

### Automated Individual Service Testing Script

Create a PowerShell script to test all services automatically:

```powershell
# test-individual-services.ps1

Write-Host "===== Testing Individual Services =====" -ForegroundColor Cyan

# Test Config Server
Write-Host "`n1. Testing Config Server (8888)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://admin:admin123@localhost:8888/actuator/health"
    if ($response.status -eq "UP") {
        Write-Host "   ✓ Config Server is UP" -ForegroundColor Green
    }
} catch {
    Write-Host "   ✗ Config Server is DOWN" -ForegroundColor Red
}

# Test Auth Service
Write-Host "`n2. Testing Auth Service (8081)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/actuator/health"
    if ($response.status -eq "UP") {
        Write-Host "   ✓ Auth Service is UP" -ForegroundColor Green
        
        # Test registration
        $registerBody = @{
            email = "testuser@example.com"
            password = "password123"
            firstName = "John"
            lastName = "Doe"
        } | ConvertTo-Json
        
        $registerResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" `
            -Method POST `
            -ContentType "application/json" `
            -Body $registerBody
        
        if ($registerResponse.token) {
            Write-Host "   ✓ User registration successful" -ForegroundColor Green
            $global:JWT_TOKEN = $registerResponse.token
        }
    }
} catch {
    Write-Host "   ✗ Auth Service test failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test Catalog Service
Write-Host "`n3. Testing Catalog Service (8082)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8082/actuator/health"
    if ($response.status -eq "UP") {
        Write-Host "   ✓ Catalog Service is UP" -ForegroundColor Green
        
        # Test get products
        $headers = @{
            Authorization = "Bearer $global:JWT_TOKEN"
        }
        $products = Invoke-RestMethod -Uri "http://localhost:8082/api/products" -Headers $headers
        Write-Host "   ✓ Retrieved $($products.Count) products" -ForegroundColor Green
    }
} catch {
    Write-Host "   ✗ Catalog Service test failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test Order Service
Write-Host "`n4. Testing Order Service (8083)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8083/actuator/health"
    if ($response.status -eq "UP") {
        Write-Host "   ✓ Order Service is UP" -ForegroundColor Green
        
        # Test create order
        $orderBody = @{
            items = @(
                @{
                    productId = "507f1f77bcf86cd799439011"
                    quantity = 2
                }
            )
        } | ConvertTo-Json
        
        $headers = @{
            Authorization = "Bearer $global:JWT_TOKEN"
        }
        $orderResponse = Invoke-RestMethod -Uri "http://localhost:8083/api/orders" `
            -Method POST `
            -ContentType "application/json" `
            -Headers $headers `
            -Body $orderBody
        
        if ($orderResponse.id) {
            Write-Host "   ✓ Order created with ID: $($orderResponse.id)" -ForegroundColor Green
        }
    }
} catch {
    Write-Host "   ✗ Order Service test failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test Gateway Service
Write-Host "`n5. Testing Gateway Service (8080)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health"
    if ($response.status -eq "UP") {
        Write-Host "   ✓ Gateway Service is UP" -ForegroundColor Green
        
        # Test routing to Auth Service
        $authHealth = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/actuator/health"
        if ($authHealth.status -eq "UP") {
            Write-Host "   ✓ Gateway routes to Auth Service" -ForegroundColor Green
        }
        
        # Test routing to Catalog Service
        $catalogHealth = Invoke-RestMethod -Uri "http://localhost:8080/api/catalog/actuator/health"
        if ($catalogHealth.status -eq "UP") {
            Write-Host "   ✓ Gateway routes to Catalog Service" -ForegroundColor Green
        }
        
        # Test routing to Order Service
        $orderHealth = Invoke-RestMethod -Uri "http://localhost:8080/api/orders/actuator/health"
        if ($orderHealth.status -eq "UP") {
            Write-Host "   ✓ Gateway routes to Order Service" -ForegroundColor Green
        }
    }
} catch {
    Write-Host "   ✗ Gateway Service test failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n===== Individual Service Testing Complete =====" -ForegroundColor Cyan
```

**Run the script:**

```powershell
.\test-individual-services.ps1
```

---

### Individual Service Testing Checklist

Use this checklist to verify each service:

#### Config Server (8888)

- [ ] Health check returns "UP"
- [ ] Can retrieve application configuration
- [ ] Can retrieve service-specific configurations
- [ ] Configuration files loaded from `file:./config-repo/`
- [ ] Basic authentication works (admin/admin123)

#### Auth Service (8081)

- [ ] Health check returns "UP" with database status
- [ ] User registration creates new user and returns JWT
- [ ] User login with valid credentials returns JWT
- [ ] Invalid credentials return 401 error
- [ ] JWT token has correct format (header.payload.signature)

#### Catalog Service (8082)

- [ ] Health check returns "UP" with MongoDB and Redis status
- [ ] GET /api/products returns product list
- [ ] GET /api/products/{id} returns specific product
- [ ] Search by category works
- [ ] Unauthorized requests return 401
- [ ] Product creation works (admin only)

#### Order Service (8083)

- [ ] Health check returns "UP" with database and circuit breaker status
- [ ] Order creation calculates totals correctly
- [ ] Order creation fetches product details from Catalog Service
- [ ] User can retrieve their orders
- [ ] Order status updates work (admin only)
- [ ] Circuit breaker state is CLOSED

#### Gateway Service (8080)

- [ ] Health check returns "UP" with Redis status
- [ ] Routes to Auth Service work
- [ ] Routes to Catalog Service work
- [ ] Routes to Order Service work
- [ ] Rate limiting triggers after burst capacity
- [ ] CORS headers present in responses
- [ ] Path prefixes stripped correctly

---

### Common Issues When Testing Individual Services

#### Issue: 401 Unauthorized from Config Server

```
curl : The remote server returned an error: (401) Unauthorized.
```

**Solution:**
Include Basic Authentication credentials:

```powershell
curl http://admin:admin123@localhost:8888/application/default
```

#### Issue: Database Connection Failed

```json
{
  "status": "DOWN",
  "components": {
    "db": {
      "status": "DOWN",
      "details": {
        "error": "Connection refused"
      }
    }
  }
}
```

**Solution:**

- Ensure PostgreSQL/MongoDB is running
- Check database credentials in configuration
- Verify database exists: `CREATE DATABASE auth_db;`

#### Issue: JWT Token Invalid

```json
{
  "error": "Invalid or expired token"
}
```

**Solution:**

- Ensure JWT secret matches across services
- Check token expiration (24 hours for access tokens)
- Verify token format (should have 3 parts separated by dots)

#### Issue: Circuit Breaker OPEN

```json
{
  "circuitBreakers": {
    "catalogService": "OPEN"
  }
}
```

**Solution:**

- Verify Catalog Service is running and healthy
- Check Order Service logs for errors calling Catalog Service
- Wait for circuit breaker to transition to HALF_OPEN (5 seconds)
- Circuit breaker opens after 50% failure rate over 10 calls

#### Issue: Rate Limit Exceeded

```json
{
  "error": "Too Many Requests"
}
```

**Solution:**

- This is expected behavior when testing rate limiting
- Wait 1 second for token bucket to replenish
- Reduce request frequency
- Check Redis connection (rate limiting requires Redis)

---

## Verify Gateway Routing

### Health Check All Services

```powershell
# Check Gateway health
curl http://localhost:8080/actuator/health

# Check Auth Service (via Gateway)
curl http://localhost:8080/auth/actuator/health

# Check Catalog Service (via Gateway)
curl http://localhost:8080/catalog/actuator/health

# Check Order Service (via Gateway)
curl http://localhost:8080/order/actuator/health
```

### Test API Endpoints Through Gateway

#### 1. Register a User (Auth Service)

```powershell
curl -X POST http://localhost:8080/auth/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "email": "testuser@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Expected Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "testuser@example.com",
  "role": "USER"
}
```

#### 2. Login User (Auth Service)

```powershell
curl -X POST http://localhost:8080/auth/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    "email": "testuser@example.com",
    "password": "password123"
  }'
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "testuser@example.com",
  "role": "USER"
}
```

**Save the JWT token for subsequent requests.**

#### 3. Get Products (Catalog Service)

```powershell
curl http://localhost:8080/catalog/api/products `
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
[
  {
    "id": "507f1f77bcf86cd799439011",
    "name": "Wireless Mouse",
    "description": "Ergonomic wireless mouse",
    "price": 29.99,
    "stock": 150,
    "category": "Electronics",
    "active": true
  }
]
```

#### 4. Create Order (Order Service)

```powershell
curl -X POST http://localhost:8080/order/api/orders `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_JWT_TOKEN" `
  -d '{
    "items": [
      {
        "productId": "507f1f77bcf86cd799439011",
        "quantity": 2
      }
    ]
  }'
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "userId": 123,
  "userEmail": "testuser@example.com",
  "items": [
    {
      "id": 1,
      "productId": "507f1f77bcf86cd799439011",
      "productName": "Wireless Mouse",
      "price": 29.99,
      "quantity": 2,
      "subtotal": 59.98
    }
  ],
  "totalAmount": 59.98,
  "status": "PENDING",
  "createdAt": "2025-01-15T10:30:00Z"
}
```

### Automated Health Check Script

```powershell
# Check all services
$services = @(
    @{Name="Config Server"; Url="http://localhost:8888/actuator/health"},
    @{Name="Auth Service"; Url="http://localhost:8081/actuator/health"},
    @{Name="Catalog Service"; Url="http://localhost:8082/actuator/health"},
    @{Name="Order Service"; Url="http://localhost:8083/actuator/health"},
    @{Name="Gateway Service"; Url="http://localhost:8080/actuator/health"}
)

foreach ($service in $services) {
    Write-Host "Checking $($service.Name)..." -ForegroundColor Yellow
    try {
        $response = Invoke-WebRequest -Uri $service.Url -UseBasicParsing
        Write-Host "✓ $($service.Name) is UP" -ForegroundColor Green
    } catch {
        Write-Host "✗ $($service.Name) is DOWN" -ForegroundColor Red
    }
}
```

---

## Docker Compose Alternative

For a simpler local setup with all dependencies, use the provided docker-compose.yml:

### Build Docker Images

```powershell
# Build all services
mvn clean package

# Build Docker images
docker-compose build
```

### Start All Services

```powershell
# Start all services with dependencies
docker-compose up
```

**What this includes:**
- ✅ Config Server
- ✅ Auth Service + PostgreSQL
- ✅ Catalog Service + MongoDB
- ✅ Order Service + PostgreSQL
- ✅ Gateway Service
- ✅ Kafka + Zookeeper
- ✅ Prometheus + Grafana (monitoring)

### Stop All Services

```powershell
docker-compose down
```

### View Logs

```powershell
# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f gateway-service
docker-compose logs -f auth-service
```

---

## Troubleshooting

### Services Not Starting

#### Issue: Java Version Mismatch
```
Error: A JNI error has occurred, please check your installation and try again
Exception in thread "main" java.lang.UnsupportedClassVersionError
```

**Solution:**
```powershell
# Check Java version
java -version

# Should show: openjdk version "17.x.x"
# If not, install Java 17 and set JAVA_HOME
```

#### Issue: Port Already in Use
```
Web server failed to start. Port 8080 was already in use.
```

**Solution:**
```powershell
# Find process using port
netstat -ano | findstr :8080

# Kill process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

#### Issue: Database Connection Failed
```
HikariPool-1 - Exception during pool initialization.
Connection refused: connect
```

**Solution:**
- Ensure PostgreSQL is running
- Check database credentials in application configuration files
- Verify database exists: `CREATE DATABASE ecommerce_auth;`

### Gateway Not Routing

#### Issue: 404 Not Found
```
{
  "timestamp": "2025-01-15T10:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/auth/api/auth/register"
}
```

**Solution:**
1. Check Gateway route configuration in gateway-service/src/main/resources/application.yml
2. Verify backend service is running: `curl http://localhost:8081/actuator/health`
3. Check Gateway logs for routing errors

#### Issue: Service Registration Failed
```
DiscoveryClient_GATEWAY-SERVICE - registration status: 503
```

**Solution:**
- Ensure Config Server is running first
- Check network connectivity between services
- Verify service discovery configuration

### Common Build Errors

#### Issue: Tests Failing
```
[ERROR] Tests run: 10, Failures: 2, Errors: 0, Skipped: 0
```

**Solution:**
```powershell
# Skip tests during build
mvn clean install -DskipTests
```

#### Issue: Dependency Resolution Failed
```
[ERROR] Failed to execute goal on project auth-service: 
Could not resolve dependencies
```

**Solution:**
```powershell
# Clear Maven cache and rebuild
mvn dependency:purge-local-repository
mvn clean install
```

---

## Service Ports Reference

| **Service** | **Port** | **Purpose** | **Access URL** |
|-------------|----------|-------------|----------------|
| **Config Server** | 8888 | Centralized configuration | http://localhost:8888 |
| **Auth Service** | 8081 | Authentication & Authorization | http://localhost:8081 |
| **Catalog Service** | 8082 | Product catalog management | http://localhost:8082 |
| **Order Service** | 8083 | Order processing | http://localhost:8083 |
| **Gateway Service** | 8080 | **Main entry point** | http://localhost:8080 |

### Gateway Route Mappings

| **Route Path** | **Backend Service** | **Example** |
|----------------|---------------------|-------------|
| `/auth/**` | Auth Service (8081) | `http://localhost:8080/auth/api/auth/login` |
| `/catalog/**` | Catalog Service (8082) | `http://localhost:8080/catalog/api/products` |
| `/order/**` | Order Service (8083) | `http://localhost:8080/order/api/orders` |

---

## Week 1 Verification Checklist

✅ **Config Server Running:** `curl http://localhost:8888/actuator/health`  
✅ **Auth Service Running:** `curl http://localhost:8081/actuator/health`  
✅ **Catalog Service Running:** `curl http://localhost:8082/actuator/health`  
✅ **Order Service Running:** `curl http://localhost:8083/actuator/health`  
✅ **Gateway Service Running:** `curl http://localhost:8080/actuator/health`  
✅ **Gateway Routes to Auth:** `curl http://localhost:8080/auth/actuator/health`  
✅ **Gateway Routes to Catalog:** `curl http://localhost:8080/catalog/actuator/health`  
✅ **Gateway Routes to Order:** `curl http://localhost:8080/order/actuator/health`  
✅ **User Registration Works:** `POST http://localhost:8080/auth/api/auth/register`  
✅ **User Login Works:** `POST http://localhost:8080/auth/api/auth/login`  
✅ **Product Retrieval Works:** `GET http://localhost:8080/catalog/api/products`  
✅ **Order Creation Works:** `POST http://localhost:8080/order/api/orders`

---

## Key Takeaways

1. **Service Order Matters:** Config Server must start first, Gateway last
2. **Gateway is Entry Point:** All client requests go to port 8080
3. **Path-Based Routing:** Gateway routes based on URL paths (`/auth/*`, `/catalog/*`, `/order/*`)
4. **Health Endpoints:** Use `/actuator/health` to verify each service
5. **JWT Authentication:** Auth service issues tokens; Gateway validates them for protected routes
6. **Docker Simplifies Setup:** Use Docker Compose for complete environment with dependencies

---

## Next Steps

After verifying local setup:

1. **Review API Contracts:** See OpenAPI-Usage-Guide.md for API specifications
2. **Test with Postman:** Import OpenAPI specs from docs/API-Specs/
3. **Monitor Services:** Access Prometheus at http://localhost:9090 (if using Docker Compose)
4. **View Metrics:** Access Grafana at http://localhost:3000 (if using Docker Compose)
5. **Deploy to Cloud:** Follow deployment guides in cloudformation/ or terraform/

---

**Document Version:** 1.0  
**Last Updated:** January 2025  
**Maintained By:** E-Commerce Development Team  
**Related Documents:**
- OpenAPI Usage Guide (docs/OpenAPI-Usage-Guide.md)
- High-Level Design (docs/HLD.md)
- Low-Level Design (docs/LLD.md)
- API Specifications (docs/API-Specs/)