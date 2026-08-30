# API Specifications - E-Commerce Microservices Platform

## Overview

This directory contains OpenAPI 3.0 specifications for all microservices in the E-Commerce platform.

## Available Specifications

1. **gateway-api.yaml** - **[RECOMMENDED]** Unified API Gateway specification (single entry point for all client
   requests)
2. **auth-service-api.yaml** - Authentication and authorization endpoints (direct service access)
3. **catalog-service-api.yaml** - Product catalog management endpoints (direct service access)
4. **order-service-api.yaml** - Order processing endpoints (direct service access)

### Which Specification Should I Use?

**For External Clients (Web/Mobile Apps):**

- Use **gateway-api.yaml** - This is the unified API contract that includes:
    - All microservice endpoints accessible through the gateway
    - Rate limiting policies per service
    - JWT authentication requirements
    - User context propagation details
    - CORS configuration
    - Gateway health and metrics endpoints

**For Internal Development/Testing:**

- Use individual service specifications (auth-service-api.yaml, catalog-service-api.yaml, order-service-api.yaml) when:
    - Testing services directly without the gateway
    - Developing service-to-service communication (e.g., Order Service → Catalog Service)
    - Debugging service-specific issues

## Viewing Specifications

You can view these specifications using:
- Swagger UI: https://editor.swagger.io/
- Postman: Import the YAML files
- VS Code: Install OpenAPI (Swagger) Editor extension

## Base URLs

### Local Development
- Gateway: http://localhost:8080
- Auth Service: http://localhost:8081
- Catalog Service: http://localhost:8082
- Order Service: http://localhost:8083

### Production (AWS)
- Gateway: http://{ALB_DNS_NAME}
- Internal services are not publicly accessible

## Authentication

All protected endpoints require JWT authentication:

```
Authorization: Bearer {JWT_TOKEN}
```

Obtain JWT token by calling:
- POST /api/auth/register
- POST /api/auth/login