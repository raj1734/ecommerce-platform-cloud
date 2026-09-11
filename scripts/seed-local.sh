#!/usr/bin/env bash
set -euo pipefail
BASE=http://localhost:8080
ADMIN_TOKEN=$(curl -fsS -X POST "$BASE/api/v1/auth/login" -H 'Content-Type: application/json' -d '{"username":"admin","password":"ChangeMe@123"}' | python3 -c 'import sys,json; print(json.load(sys.stdin)["accessToken"])')
PRODUCT=$(curl -fsS -X POST "$BASE/api/v1/products" -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' -d '{"sku":"PHONE-001","name":"Smartphone X","description":"Example smartphone","category":"Electronics","brand":"ExampleBrand","price":{"amount":49999.00,"currency":"INR"}}')
PRODUCT_ID=$(printf '%s' "$PRODUCT" | python3 -c 'import sys,json; print(json.load(sys.stdin)["id"])')
curl -fsS -X POST "$BASE/api/v1/inventory" -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' -d "{\"productId\":\"$PRODUCT_ID\",\"sku\":\"PHONE-001\",\"availableQuantity\":25,\"reorderLevel\":5}" >/dev/null

curl -fsS -X POST "$BASE/api/v1/auth/register" -H 'Content-Type: application/json' -d '{"username":"john.doe","email":"john@example.com","password":"Password@123"}' >/dev/null || true
USER_TOKEN=$(curl -fsS -X POST "$BASE/api/v1/auth/login" -H 'Content-Type: application/json' -d '{"username":"john.doe","password":"Password@123"}' | python3 -c 'import sys,json; print(json.load(sys.stdin)["accessToken"])')
curl -fsS "$BASE/api/v1/users/me" -H "Authorization: Bearer $USER_TOKEN" >/dev/null
ADDRESS=$(curl -fsS -X POST "$BASE/api/v1/users/me/addresses" -H "Authorization: Bearer $USER_TOKEN" -H 'Content-Type: application/json' -d '{"addressLine1":"123 Main Street","city":"Hyderabad","state":"Telangana","postalCode":"500001","country":"IN","isDefault":true}')
ADDRESS_ID=$(printf '%s' "$ADDRESS" | python3 -c 'import sys,json; print(json.load(sys.stdin)["id"])')
CART=$(curl -fsS -X POST "$BASE/api/v1/cart/items" -H "Authorization: Bearer $USER_TOKEN" -H 'Content-Type: application/json' -d "{\"productId\":\"$PRODUCT_ID\",\"quantity\":2}")
CART_ID=$(printf '%s' "$CART" | python3 -c 'import sys,json; print(json.load(sys.stdin)["cartId"])')
ORDER=$(curl -fsS -X POST "$BASE/api/v1/orders/checkout" -H "Authorization: Bearer $USER_TOKEN" -H 'Content-Type: application/json' -H 'Idempotency-Key: local-checkout-001' -d "{\"cartId\":\"$CART_ID\",\"shippingAddressId\":\"$ADDRESS_ID\",\"paymentMethod\":\"CARD\"}")
printf 'Product: %s\nOrder: %s\n' "$PRODUCT_ID" "$(printf '%s' "$ORDER" | python3 -c 'import sys,json; print(json.load(sys.stdin).get("orderNumber"))')"
