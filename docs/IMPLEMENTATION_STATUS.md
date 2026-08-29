# Implementation status

The repository is aligned to the supplied LLD for the core API/database contracts:

- Auth Service: UUID identity, username/email, BCrypt password hashes, USER/ADMIN roles, JWT issuance/validation.
- User Service: profile, addresses and preferences with UUID references to Auth Service.
- Catalog Service: MongoDB `products` document model, cache-aside Redis, product status/price snapshots.
- Inventory Service: PostgreSQL inventory/reservation model with optimistic locking and reservation lifecycle.
- Cart & Order Service: PostgreSQL cart/order model, idempotent checkout, status history, synchronous Inventory/Payment calls and Kafka `OrderCreated`.
- Payment and Notification remain stubs as explicitly defined by the LLD.
- Gateway: JWT validation, correlation ID propagation, routing and Redis rate-limit key resolution.
- Config Server: native configuration repository for local development.
- Docker Compose: complete local dependency stack.
- Terraform: AWS VPC/private networking, RDS PostgreSQL, MSK, ECS cluster and CloudWatch foundation.

## Important production hardening

The supplied LLD intentionally skips Outbox/Saga and external payment providers. Before production checkout, add an Outbox pattern, a real payment provider, secret delivery via AWS Secrets Manager, TLS/private MSK configuration, task definitions/service discovery, and CI/CD image publishing.
