# Web Storefront - Spring MVC + Thymeleaf + Bootstrap

This is a standalone UI module for the Distributed E-Commerce Platform.

## API contract used

The module is aligned to the supplied OpenAPI specification:
- Gateway server: `http://localhost:8080`
- API base: `/api/v1`
- Bearer JWT authentication
- Auth, users, products, inventory, cart, orders, payments and notifications endpoints
- `Idempotency-Key` and `X-Correlation-ID` are sent where applicable

## Run standalone

```bash
mvn clean spring-boot:run
```

Open http://localhost:8090

Override the Gateway:

```bash
GATEWAY_URL=http://localhost:8080 mvn spring-boot:run
```

Windows PowerShell:

```powershell
$env:GATEWAY_URL="http://localhost:8080"
mvn spring-boot:run
```

## Add to existing Maven multi-module project

Copy `web-storefront` into the repository root and add:

```xml
<module>web-storefront</module>
```

to the parent `<modules>` list.

The UI calls the Gateway only; it does not call individual microservices directly.

## Screens

- Login / registration
- Product catalogue and search
- Product detail
- Add/update/remove cart items
- Checkout with address and payment method
- Order history
- Order detail and cancellation
- Profile and addresses
- Admin product create/edit/status
- Inventory view

## Important contract note

The supplied OpenAPI spec does not define response schemas for most endpoints. The UI therefore reads common JSON field names defensively (`id`/`productId`, `token`/`accessToken`, `content`/`items`, etc.). If the backend response shapes differ, update the small controller/template mappings rather than changing the API contract.
