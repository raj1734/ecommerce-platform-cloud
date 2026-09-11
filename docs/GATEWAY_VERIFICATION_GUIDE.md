# API Gateway Routing Verification Guide

**Document Version:** 1.0  
**Last Updated:** August 2026  
**Project:** Distributed E-Commerce Microservices Platform  
**Purpose:** Comprehensive guide to verify API Gateway can reach all backend services

---

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Quick Start](#quick-start)
4. [Manual Verification Steps](#manual-verification-steps)
5. [Automated Verification Scripts](#automated-verification-scripts)
6. [Gateway Routing Configuration](#gateway-routing-configuration)
7. [Troubleshooting](#troubleshooting)
8. [Expected Results](#expected-results)

---

## Overview

### What This Guide Covers

This guide provides comprehensive instructions to verify that the **API Gateway** (running on port 8080) can successfully route requests to all backend microservices:

- **Auth Service** (port 8081) - Authentication and authorization
- **Catalog Service** (port 8082) - Product catalog management
- **Order Service** (port 8083) - Order processing

### Architecture Flow

```
Client Request
      |
      v
API Gateway (8080)
      |
      +---> /api/auth/**    ---> Auth Service (8081)
      |
      +---> /api/catalog/** ---> Catalog Service (8082)
      |
      +---> /api/orders/**  ---> Order Service (8083)
```

### Gateway Routing Rules

| Route Path | Backend Service | Port | Strip Prefix | Example |
|------------|----------------|------|--------------|----------|
| `/api/auth/**` | auth-service | 8081 | 2 segments | `http://localhost:8080/api/auth/login` → `http://localhost:8081/login` |
| `/api/catalog/**` | catalog-service | 8082 | 2 segments | `http://localhost:8080/api/catalog/products` → `http://localhost:8082/products` |
| `/api/orders/**` | order-service | 8083 | 2 segments | `http://localhost:8080/api/orders` → `http://localhost:8083/` |

**Note:** The `StripPrefix=2` filter removes the first two path segments (`/api/auth`, `/api/catalog`, `/api/orders`) before forwarding to backend services.

---

## Prerequisites

### Required Services Running

Before verifying gateway routing, ensure all services are running:

#### 1. Config Server (Port 8888)

```powershell
cd config-server
mvn spring-boot:run
```

**Verify:**
```powershell
curl http://localhost:8888/actuator/health
```

**Expected Response:**
```json
{"status":"UP"}
```

#### 2. Auth Service (Port 8081)

```powershell
cd auth-service
mvn spring-boot:run
```

**Verify:**
```powershell
curl http://localhost:8081/actuator/health
```

**Expected Response:**
```json
{"status":"UP"}
```

#### 3. Catalog Service (Port 8082)

```powershell
cd catalog-service
mvn spring-boot:run
```

**Verify:**
```powershell
curl http://localhost:8082/actuator/health
```

**Expected Response:**
```json
{"status":"UP"}
```

#### 4. Order Service (Port 8083)

```powershell
cd order-service
mvn spring-boot:run
```

**Verify:**
```powershell
curl http://localhost:8083/actuator/health
```

**Expected Response:**
```json
{"status":"UP"}
```

#### 5. Gateway Service (Port 8080)

```powershell
cd gateway-service
mvn spring-boot:run
```

**Verify:**
```powershell
curl http://localhost:8080/actuator/health
```

**Expected Response:**
```json
{"status":"UP"}
```

### Optional: Redis (For Rate Limiting)

If testing rate limiting features:

```powershell
# Windows (using Docker)
docker run -d -p 6379:6379 redis:latest

# Linux/macOS
redis-server
```

**Verify:**
```powershell
redis-cli ping
```

**Expected Response:**
```
PONG
```

---

## Quick Start

### Option 1: Automated Verification Script (Recommended)

#### For Windows (PowerShell)

```powershell
# Run the verification script
.\verify-gateway-routing.ps1
```

#### For Linux/macOS (Bash)

```bash
# Make script executable
chmod +x verify-gateway-routing.sh

# Run the verification script
./verify-gateway-routing.sh
```

**What the script does:**
1. ✓ Verifies Gateway service is running
2. ✓ Verifies all backend services are running (direct access)
3. ✓ Tests Gateway routing to each backend service
4. ✓ Tests authentication flow through Gateway
5. ✓ Tests catalog service access with JWT token
6. ✓ Tests order service access with JWT token
7. ✓ Tests CORS configuration
8. ✓ Tests rate limiting (if Redis is running)

**Expected Output:**
```
========================================
  Verification Summary
========================================

Total Tests: 15
Passed: 15
Failed: 0
Pass Rate: 100.00%

✓ All gateway routing tests passed!
ℹ The API Gateway can successfully reach all backend services.
```

### Option 2: Manual Verification (Step-by-Step)

See [Manual Verification Steps](#manual-verification-steps) section below.

---

## Manual Verification Steps

### Step 1: Verify Gateway is Running

```powershell
curl http://localhost:8080/actuator/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "components": {
    "redis": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

**✓ Success Criteria:**
- Status is "UP"
- No connection errors
- Response received within 2 seconds

---

### Step 2: Verify Backend Services (Direct Access)

Test each backend service directly on their respective ports:

#### Auth Service (Port 8081)

```powershell
curl http://localhost:8081/actuator/health
```

**Expected Response:**
```json
{"status":"UP"}
```

#### Catalog Service (Port 8082)

```powershell
curl http://localhost:8082/actuator/health
```

**Expected Response:**
```json
{"status":"UP"}
```

#### Order Service (Port 8083)

```powershell
curl http://localhost:8083/actuator/health
```

**Expected Response:**
```json
{"status":"UP"}
```

**✓ Success Criteria:**
- All three services return status "UP"
- No connection refused errors
- Services respond on their designated ports

---

### Step 3: Test Gateway Routing to Backend Services

Test that Gateway can route health check requests to each backend service:

#### Gateway → Auth Service

```powershell
curl http://localhost:8080/api/auth/actuator/health
```

**Expected Response:**
```json
{"status":"UP"}
```

**What happens:**
1. Request sent to Gateway at `http://localhost:8080/api/auth/actuator/health`
2. Gateway matches route predicate: `Path=/api/auth/**`
3. Gateway strips prefix (removes `/api/auth`)
4. Gateway forwards to Auth Service: `http://localhost:8081/actuator/health`
5. Auth Service responds with health status
6. Gateway returns response to client

#### Gateway → Catalog Service

```powershell
curl http://localhost:8080/api/catalog/actuator/health
```

**Expected Response:**
```json
{"status":"UP"}
```

**What happens:**
1. Request sent to Gateway at `http://localhost:8080/api/catalog/actuator/health`
2. Gateway matches route predicate: `Path=/api/catalog/**`
3. Gateway strips prefix (removes `/api/catalog`)
4. Gateway forwards to Catalog Service: `http://localhost:8082/actuator/health`
5. Catalog Service responds with health status
6. Gateway returns response to client

#### Gateway → Order Service

```powershell
curl http://localhost:8080/api/orders/actuator/health
```

**Expected Response:**
```json
{"status":"UP"}
```

**What happens:**
1. Request sent to Gateway at `http://localhost:8080/api/orders/actuator/health`
2. Gateway matches route predicate: `Path=/api/orders/**`
3. Gateway strips prefix (removes `/api/orders`)
4. Gateway forwards to Order Service: `http://localhost:8083/actuator/health`
5. Order Service responds with health status
6. Gateway returns response to client

**✓ Success Criteria:**
- All three routes return status "UP"
- Gateway successfully routes to each backend service
- No 404 Not Found errors
- No 503 Service Unavailable errors

---

### Step 4: Test Authentication Flow Through Gateway

#### Register a New User

```powershell
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "email": "testuser@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
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

#### Login User

```powershell
curl -X POST http://localhost:8080/api/auth/login `
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

**✓ Success Criteria:**
- User registration returns JWT token
- User login with correct credentials returns JWT token
- Requests routed through Gateway to Auth Service successfully
- JWT token format is valid (three base64-encoded parts separated by dots)

---

### Step 5: Test Catalog Service Access Through Gateway

#### Get All Products (Authenticated)

```powershell
curl http://localhost:8080/api/catalog/products `
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
[
  {
    "id": "1",
    "name": "Wireless Mouse",
    "description": "Ergonomic wireless mouse",
    "price": 29.99,
    "stock": 150,
    "category": "Electronics",
    "active": true
  }
]
```

#### Get Product by ID

```powershell
curl http://localhost:8080/api/catalog/products/1 `
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "id": "1",
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse",
  "price": 29.99,
  "stock": 150,
  "category": "Electronics",
  "active": true
}
```

**✓ Success Criteria:**
- Products retrieved successfully through Gateway
- JWT token validated by Gateway or backend service
- Requests routed from Gateway to Catalog Service
- Response data matches expected format

---

### Step 6: Test Order Service Access Through Gateway

#### Get User Orders (Authenticated)

```powershell
curl http://localhost:8080/api/orders `
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
    "createdAt": "2026-08-07T10:30:00Z"
  }
]
```

#### Create Order (Authenticated)

```powershell
curl -X POST http://localhost:8080/api/orders `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_JWT_TOKEN" `
  -d '{
    "items": [
      {
        "productId": "1",
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
      "productId": "1",
      "productName": "Wireless Mouse",
      "price": 29.99,
      "quantity": 2,
      "subtotal": 59.98
    }
  ],
  "totalAmount": 59.98,
  "status": "PENDING",
  "createdAt": "2026-08-07T10:30:00Z"
}
```

**✓ Success Criteria:**
- Orders retrieved successfully through Gateway
- Order creation works through Gateway
- JWT token validated correctly
- Requests routed from Gateway to Order Service
- Order Service can call Catalog Service to fetch product details

---

### Step 7: Test CORS Configuration

#### CORS Preflight Request

```powershell
curl -X OPTIONS http://localhost:8080/api/catalog/products `
  -H "Origin: http://localhost:3000" `
  -H "Access-Control-Request-Method: GET" `
  -v
```

**Expected Response Headers:**
```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Max-Age: 3600
```

**✓ Success Criteria:**
- CORS headers present in response
- `Access-Control-Allow-Origin` header set to `*` (or specific origin)
- `Access-Control-Allow-Methods` includes GET, POST, PUT, DELETE, OPTIONS
- `Access-Control-Allow-Headers` set to `*`

---

### Step 8: Test Rate Limiting (Optional)

**Note:** Requires Redis to be running.

#### Send Multiple Requests Rapidly

```powershell
# PowerShell
for ($i = 1; $i -le 25; $i++) {
    Write-Host "Request $i"
    curl http://localhost:8080/api/catalog/products `
      -H "Authorization: Bearer YOUR_JWT_TOKEN"
    Start-Sleep -Milliseconds 100
}
```

**Expected Behavior:**
- First 20 requests succeed (burst capacity for catalog service)
- Requests 21-25 may return **429 Too Many Requests** if rate limit exceeded

**Expected Response (429 Too Many Requests):**
```json
{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Try again later."
}
```

**✓ Success Criteria:**
- Rate limiting triggers after burst capacity
- 429 status code returned when limit exceeded
- Redis connection is working
- Rate limiting configuration is applied correctly

---

## Automated Verification Scripts

### PowerShell Script (Windows)

**File:** `verify-gateway-routing.ps1`

**Usage:**
```powershell
.\verify-gateway-routing.ps1
```

**Features:**
- ✓ Colored output (success, failure, info, warning)
- ✓ Comprehensive test coverage (8 test categories)
- ✓ Automatic JWT token handling
- ✓ Test result summary with pass rate
- ✓ Exit codes (0 = success, 1 = failure)

**Test Categories:**
1. Gateway service health check
2. Backend services health checks (direct access)
3. Gateway routing to backend services
4. Authentication flow through gateway
5. Catalog service access with JWT
6. Order service access with JWT
7. CORS configuration
8. Rate limiting (optional)

### Bash Script (Linux/macOS)

**File:** `verify-gateway-routing.sh`

**Usage:**
```bash
chmod +x verify-gateway-routing.sh
./verify-gateway-routing.sh
```

**Features:**
- ✓ Colored output (success, failure, info, warning)
- ✓ Comprehensive test coverage (8 test categories)
- ✓ Automatic JWT token handling
- ✓ Test result summary with pass rate
- ✓ Exit codes (0 = success, 1 = failure)

**Same test categories as PowerShell script.**

---

## Gateway Routing Configuration

### Configuration File

**File:** `gateway-service/src/main/resources/application.yml`

### Route Definitions

```yaml
spring:
  cloud:
    gateway:
      routes:
        # Auth Service Route
        - id: auth-service
          uri: lb://auth-service  # Load-balanced URI
          predicates:
            - Path=/api/auth/**   # Match requests to /api/auth/**
          filters:
            - StripPrefix=2       # Remove /api/auth from path
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
        
        # Catalog Service Route
        - id: catalog-service
          uri: lb://catalog-service
          predicates:
            - Path=/api/catalog/**
          filters:
            - StripPrefix=2
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 50
                redis-rate-limiter.burstCapacity: 100
        
        # Order Service Route
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=2
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 20
                redis-rate-limiter.burstCapacity: 40
```

### Key Configuration Elements

#### Load Balancing

```yaml
uri: lb://auth-service
```

- `lb://` prefix enables client-side load balancing
- Service name (`auth-service`) resolved via service discovery
- Supports multiple instances of the same service

#### Path Predicates

```yaml
predicates:
  - Path=/api/auth/**
```

- Matches incoming requests based on URL path
- `**` matches any number of path segments
- Multiple predicates can be combined with AND logic

#### StripPrefix Filter

```yaml
filters:
  - StripPrefix=2
```

- Removes specified number of path segments before forwarding
- `StripPrefix=2` removes first two segments
- Example: `/api/auth/login` → `/login`

#### Rate Limiting

```yaml
- name: RequestRateLimiter
  args:
    redis-rate-limiter.replenishRate: 10
    redis-rate-limiter.burstCapacity: 20
```

- **replenishRate:** Tokens added per second (sustained rate)
- **burstCapacity:** Maximum tokens in bucket (burst capacity)
- Requires Redis to be running
- Returns 429 when limit exceeded

#### CORS Configuration

```yaml
globalcors:
  corsConfigurations:
    '[/**]':
      allowedOrigins: "*"
      allowedMethods:
        - GET
        - POST
        - PUT
        - DELETE
        - OPTIONS
      allowedHeaders: "*"
```

- Applies to all routes (`[/**]`)
- Allows all origins (`*`)
- Supports common HTTP methods
- Allows all headers

---

## Troubleshooting

### Issue 1: Gateway Returns 404 Not Found

**Symptom:**
```json
{
  "timestamp": "2026-08-07T10:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/api/auth/login"
}
```

**Possible Causes:**
1. Gateway route configuration incorrect
2. Backend service not running
3. Service discovery not working
4. Path predicate doesn't match request URL

**Solutions:**

1. **Check Gateway route configuration:**
   ```powershell
   # View gateway routes
   curl http://localhost:8080/actuator/gateway/routes
   ```

2. **Verify backend service is running:**
   ```powershell
   curl http://localhost:8081/actuator/health
   ```

3. **Check Gateway logs:**
   ```powershell
   # Look for routing errors
   cd gateway-service
   mvn spring-boot:run
   ```

4. **Verify path predicate:**
   - Ensure request path matches route predicate
   - Check for typos in path segments

---

### Issue 2: Gateway Returns 503 Service Unavailable

**Symptom:**
```json
{
  "timestamp": "2026-08-07T10:30:00.000+00:00",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Unable to find instance for auth-service"
}
```

**Possible Causes:**
1. Backend service not running
2. Service discovery not configured
3. Backend service not registered with discovery server
4. Network connectivity issues

**Solutions:**

1. **Start backend service:**
   ```powershell
   cd auth-service
   mvn spring-boot:run
   ```

2. **Check service health:**
   ```powershell
   curl http://localhost:8081/actuator/health
   ```

3. **Verify service registration:**
   - Check if service registered with discovery server
   - Verify service name matches route configuration

4. **Check network connectivity:**
   ```powershell
   # Test direct connection
   curl http://localhost:8081/actuator/health
   ```

---

### Issue 3: Rate Limiting Not Working

**Symptom:**
- Sending 100 requests doesn't trigger 429 error
- No rate limiting applied

**Possible Causes:**
1. Redis not running
2. Redis connection configuration incorrect
3. Rate limiter filter not applied to route

**Solutions:**

1. **Start Redis:**
   ```powershell
   # Windows (Docker)
   docker run -d -p 6379:6379 redis:latest
   
   # Linux/macOS
   redis-server
   ```

2. **Verify Redis connection:**
   ```powershell
   redis-cli ping
   # Expected: PONG
   ```

3. **Check Gateway Redis configuration:**
   ```yaml
   spring:
     redis:
       host: localhost
       port: 6379
   ```

4. **Verify rate limiter filter in route:**
   ```yaml
   filters:
     - name: RequestRateLimiter
       args:
         redis-rate-limiter.replenishRate: 10
         redis-rate-limiter.burstCapacity: 20
   ```

---

### Issue 4: CORS Errors in Browser

**Symptom:**
```
Access to fetch at 'http://localhost:8080/api/catalog/products' from origin 'http://localhost:3000' 
has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
```

**Possible Causes:**
1. CORS not configured in Gateway
2. CORS configuration doesn't allow origin
3. Preflight request failing

**Solutions:**

1. **Check CORS configuration:**
   ```yaml
   spring:
     cloud:
       gateway:
         globalcors:
           corsConfigurations:
             '[/**]':
               allowedOrigins: "*"
               allowedMethods:
                 - GET
                 - POST
                 - PUT
                 - DELETE
                 - OPTIONS
               allowedHeaders: "*"
   ```

2. **Test CORS preflight:**
   ```powershell
   curl -X OPTIONS http://localhost:8080/api/catalog/products `
     -H "Origin: http://localhost:3000" `
     -H "Access-Control-Request-Method: GET" `
     -v
   ```

3. **Check response headers:**
   - Verify `Access-Control-Allow-Origin` header present
   - Verify `Access-Control-Allow-Methods` includes requested method

---

### Issue 5: JWT Token Not Validated

**Symptom:**
- Requests with invalid JWT token succeed
- Requests without JWT token succeed when they should fail

**Possible Causes:**
1. JWT validation not configured in Gateway
2. JWT filter not applied to routes
3. JWT secret mismatch between services

**Solutions:**

1. **Check JWT configuration in Gateway:**
   ```yaml
   jwt:
     secret: ${JWT_SECRET:your-256-bit-secret-key-change-this-in-production}
     expiration: 86400000
   ```

2. **Verify JWT filter implementation:**
   - Check `JwtAuthenticationFilter` class
   - Ensure filter is registered in Spring Security configuration

3. **Verify JWT secret matches across services:**
   - Auth Service generates tokens with secret
   - Gateway validates tokens with same secret
   - Secrets must match exactly

---

## Expected Results

### Successful Verification Checklist

After running verification tests, you should see:

- [x] **Gateway Health Check:** Status "UP"
- [x] **Auth Service Health (Direct):** Status "UP"
- [x] **Catalog Service Health (Direct):** Status "UP"
- [x] **Order Service Health (Direct):** Status "UP"
- [x] **Gateway → Auth Service:** Health check returns "UP"
- [x] **Gateway → Catalog Service:** Health check returns "UP"
- [x] **Gateway → Order Service:** Health check returns "UP"
- [x] **User Registration:** Returns JWT token
- [x] **User Login:** Returns JWT token
- [x] **Get Products (Authenticated):** Returns product list
- [x] **Get Orders (Authenticated):** Returns order list
- [x] **CORS Preflight:** Returns CORS headers
- [x] **Rate Limiting:** Returns 429 after burst capacity (if Redis running)

### Automated Script Expected Output

```
========================================
  Gateway Routing Verification
========================================

ℹ This script verifies that the API Gateway can reach all backend services
ℹ Gateway URL: http://localhost:8080
ℹ Date: 2026-08-07 10:30:00

========================================
  Step 1: Verify Gateway Service
========================================

Testing: Gateway Health Check
  URL: http://localhost:8080/actuator/health
✓ Gateway Health Check - Status: 200

========================================
  Step 2: Verify Backend Services (Direct Access)
========================================

Testing: Auth Service Health (Direct)
  URL: http://localhost:8081/actuator/health
✓ Auth Service Health (Direct) - Status: 200

Testing: Catalog Service Health (Direct)
  URL: http://localhost:8082/actuator/health
✓ Catalog Service Health (Direct) - Status: 200

Testing: Order Service Health (Direct)
  URL: http://localhost:8083/actuator/health
✓ Order Service Health (Direct) - Status: 200

========================================
  Step 3: Verify Gateway Routing to Backend Services
========================================

ℹ Testing Gateway → Auth Service routing (Path: /api/auth/**)...
Testing: Gateway → Auth Service Health
  URL: http://localhost:8080/api/auth/actuator/health
✓ Gateway → Auth Service Health - Status: 200

ℹ Testing Gateway → Catalog Service routing (Path: /api/catalog/**)...
Testing: Gateway → Catalog Service Health
  URL: http://localhost:8080/api/catalog/actuator/health
✓ Gateway → Catalog Service Health - Status: 200

ℹ Testing Gateway → Order Service routing (Path: /api/orders/**)...
Testing: Gateway → Order Service Health
  URL: http://localhost:8080/api/orders/actuator/health
✓ Gateway → Order Service Health - Status: 200

========================================
  Step 4: Test Authentication Flow Through Gateway
========================================

ℹ Attempting user registration through gateway...
✓ User registration successful through gateway

========================================
  Step 5: Test Catalog Service Access Through Gateway
========================================

ℹ Fetching products through gateway with JWT token...
✓ Successfully retrieved products through gateway
  Products count: 5

========================================
  Step 6: Test Order Service Access Through Gateway
========================================

ℹ Fetching orders through gateway with JWT token...
✓ Successfully retrieved orders through gateway
  Orders count: 0

========================================
  Step 7: Test CORS Configuration
========================================

ℹ Testing CORS preflight request...
✓ CORS headers present in response
  Access-Control-Allow-Origin: *

========================================
  Step 8: Test Rate Limiting (Optional)
========================================

ℹ Testing rate limiting by sending multiple requests...
⚠ Note: This test requires Redis to be running
  Request 1 : Success (Status: 200)
✓ Rate limiting is working - received 429 after 21 requests
✓ Rate limiting test passed

========================================
  Verification Summary
========================================

Total Tests: 15
Passed: 15
Failed: 0
Pass Rate: 100.00%

✓ All gateway routing tests passed!
ℹ The API Gateway can successfully reach all backend services.
```

---

## Summary

This guide provides comprehensive instructions to verify that the API Gateway can successfully route requests to all backend microservices. Use the automated verification scripts for quick validation, or follow the manual steps for detailed testing.

**Key Points:**
- Gateway runs on port 8080 and routes to backend services on ports 8081-8083
- Path-based routing with prefix stripping enables clean URLs
- Rate limiting requires Redis to be running
- CORS is configured globally for all routes
- JWT tokens are used for authentication and authorization

**Next Steps:**
- Review [Local Setup Guide](Local-Setup-Guide.md) for complete environment setup
- Check [OpenAPI Usage Guide](OpenAPI-Usage-Guide.md) for API specifications
- See [HLD](HLD.md) and [LLD](LLD.md) for architecture details

---

**Document Version:** 1.0  
**Last Updated:** August 2026  
**Maintained By:** E-Commerce Development Team  
**Related Documents:**
- Local Setup Guide (docs/Local-Setup-Guide.md)
- OpenAPI Usage Guide (docs/OpenAPI-Usage-Guide.md)
- High-Level Design (docs/HLD.md)
- Low-Level Design (docs/LLD.md)
