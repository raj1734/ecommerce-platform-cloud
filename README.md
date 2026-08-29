# Distributed E-Commerce Microservices Platform

A Spring Boot/Spring Cloud implementation of the supplied e-commerce problem statement and LLD. The repository is intended to be runnable locally with Docker Compose and provides an AWS/Terraform foundation for ECS Fargate, RDS PostgreSQL and MSK.

## Services

| Service | Port | Responsibility | Store |
|---|---:|---|---|
| gateway-service | 8080 | Routing, JWT validation, correlation IDs, Redis rate limiting | Redis |
| auth-service | 8081 | Registration, login, JWT, RBAC | PostgreSQL |
| catalog-service | 8082 | Product catalogue + cache-aside | MongoDB + Redis |
| order-service | 8083 | Cart + checkout + orders | PostgreSQL |
| notification-service | 8084 | Kafka notification stub | None |
| inventory-service | 8086 | Inventory/reservations | PostgreSQL |
| user-service | 8087 | Profile/address/preferences | PostgreSQL |
| payment-service | 8088 | Payment/refund stub | None |
| config-server | 8888 | Centralized configuration | Git/native filesystem |

## LLD alignment

The implementation follows the attached LLD's Database-per-Service, UUID identifiers, idempotency, optimistic locking, resource ownership, JWT/RBAC, synchronous REST/Feign, asynchronous Kafka, Redis cache-aside, and Resilience4j principles. Payment and Notification remain stubs because the LLD explicitly defines them as such. The LLD also intentionally skips Outbox/Saga complexity for the current scope.

## Run locally

Prerequisites: Java 17+, Docker Desktop with Compose, and at least 8 GB RAM available to Docker.

```bash
docker compose up --build
```

Useful endpoints:

- Gateway: `http://localhost:8080`
- Swagger/OpenAPI YAML: `docs/API-Specs/`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (admin/admin for the local compose setup)
- Config Server: `http://localhost:8888`

### Basic flow

1. `POST /api/v1/auth/register`
2. `POST /api/v1/auth/login` and copy the Bearer token.
3. `POST /api/v1/products` with an ADMIN token to create products, or seed catalog data manually.
4. Add inventory records for the product IDs/SKUs.
5. `POST /api/v1/cart/items` to add a product to the authenticated cart.
6. `POST /api/v1/orders/checkout` with `Idempotency-Key` and a shipping address ID.
7. Watch the `OrderCreated` Kafka event in the order/notification logs.

## AWS/Terraform

See `terraform/README.md`. Terraform provisions the AWS networking and managed platform foundation and creates ECR repositories/task definitions for the service images. Application image publishing and full service-to-service discovery should be wired into CI/CD before production use.

## Production hardening still expected

- AWS Secrets Manager instead of plaintext Terraform/Compose credentials.
- Separate production databases per service (or separate logical DBs on a managed PostgreSQL platform).
- TLS/private networking for MSK and application ingress.
- CI/CD image build, ECR push and ECS rolling deployments.
- Service discovery for ECS-to-ECS URLs.
- Outbox/Saga if checkout reliability requirements exceed the intentionally simplified LLD scope.
- Real payment provider integration and notification providers.
- Flyway/Liquibase migrations for immutable production schemas.
