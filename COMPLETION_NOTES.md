# Completion Notes

This package is the completed application side of the Distributed E-Commerce Microservices Platform.

## Included

- Gateway is the single API entry point for the storefront.
- `web-storefront` is the Thymeleaf UI and calls only the Gateway.
- `OrderCreated` is published after successful checkout.
- Inventory and Notification consume `OrderCreated` from Kafka.
- Catalog lookup, inventory reservation, and payment are protected by Resilience4j Retry + Circuit Breaker in a dedicated Spring bean so AOP is applied correctly.
- Redis cache-aside is enabled for Catalog production configuration.
- Config Server packages the configuration repository into its container image for ECS/native deployment.
- Production configurations use ECS Cloud Map service DNS names instead of `localhost`.
- AWS MSK IAM authentication settings are included for Kafka services.
- Micrometer Tracing + Zipkin exporter configuration is included; local Compose includes Zipkin.
- Prometheus and Grafana remain part of the local observability stack.
- Temporary storefront/inventory copies and IDE metadata were removed from the deliverable.

## Local entry points

- Storefront: `http://localhost:8090`
- Gateway: `http://localhost:8080`
- Config Server: `http://localhost:8889`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Zipkin: `http://localhost:9411`

The storefront uses `GATEWAY_URL` and never calls backend microservices directly.
