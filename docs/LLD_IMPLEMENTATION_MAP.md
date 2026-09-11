# LLD → Implementation map

| LLD area | Implementation |
|---|---|
| Database-per-Service | Auth/User/Order/Inventory use separate PostgreSQL databases in Compose; Catalog uses MongoDB. |
| Auth | `auth-service`: UUID users, username/email, BCrypt, roles, JWT. |
| User | `user-service`: profile, addresses, preferences, Auth UUID reference without cross-DB FK. |
| Catalog | `catalog-service`: Mongo `products`, price object, status, attributes, indexes, Redis cache-aside. |
| Inventory | `inventory-service`: inventory + reservations, optimistic locking, reserve/release/consume. |
| Cart & Order | `order-service`: cart/order/order-item/status-history in one PostgreSQL database. |
| Idempotency | Checkout, inventory reservation, payment and refund use idempotency keys/guards. |
| Synchronous calls | OpenFeign from Order to Catalog/Inventory/Payment/User, Resilience4j for downstream failure. |
| Async | `OrderCreated` is published to Kafka after successful checkout; Inventory/Notification listeners are wired as local downstream consumers. |
| Gateway | JWT validation, user/role headers, correlation ID and Redis-backed RequestRateLimiter. |
| Observability | Actuator + Prometheus endpoints, Grafana provisioning, MDC correlation ID filters. |
| Cloud | Terraform foundation for VPC, RDS, MSK, ECS/Fargate, ECR, ALB and CloudWatch. |

Payment and Notification remain intentionally lightweight stubs, consistent with the LLD's current scope.
