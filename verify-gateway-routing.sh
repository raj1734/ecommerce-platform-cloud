#!/bin/bash

# Gateway Routing Verification Script (Bash)
# Purpose: Verify that API Gateway can reach all backend services
# Author: E-Commerce Development Team
# Last Updated: August 2026

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# Test results tracking
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Helper functions
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_failure() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${CYAN}ℹ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_section() {
    echo -e "\n${MAGENTA}========================================${NC}"
    echo -e "${MAGENTA}  $1${NC}"
    echo -e "${MAGENTA}========================================${NC}\n"
}

# Test endpoint function
test_endpoint() {
    local name="$1"
    local url="$2"
    local method="${3:-GET}"
    local expected_status="${4:-200}"
    local headers="${5:-}"
    local body="${6:-}"
    
    ((TOTAL_TESTS++))
    
    echo -e "${YELLOW}Testing: $name${NC}"
    echo -e "${NC}  URL: $url${NC}"
    
    # Build curl command
    local curl_cmd="curl -s -w '\n%{http_code}' -X $method '$url'"
    
    if [ -n "$headers" ]; then
        curl_cmd="$curl_cmd $headers"
    fi
    
    if [ -n "$body" ]; then
        curl_cmd="$curl_cmd -H 'Content-Type: application/json' -d '$body'"
    fi
    
    # Execute curl and capture response
    local response=$(eval $curl_cmd 2>/dev/null)
    local status_code=$(echo "$response" | tail -n1)
    
    if [ "$status_code" = "$expected_status" ]; then
        print_success "$name - Status: $status_code"
        ((PASSED_TESTS++))
        return 0
    else
        print_failure "$name - Expected: $expected_status, Got: $status_code"
        ((FAILED_TESTS++))
        return 1
    fi
}

# Main verification script
print_section "Gateway Routing Verification"
print_info "This script verifies that the API Gateway can reach all backend services"
print_info "Gateway URL: http://localhost:8080"
print_info "Date: $(date '+%Y-%m-%d %H:%M:%S')"

# Step 1: Verify Gateway is running
print_section "Step 1: Verify Gateway Service"
if test_endpoint "Gateway Health Check" "http://localhost:8080/actuator/health"; then
    GATEWAY_RUNNING=true
else
    GATEWAY_RUNNING=false
    print_failure "Gateway service is not running on port 8080"
    print_warning "Please start the gateway service first: cd gateway-service && mvn spring-boot:run"
    exit 1
fi

# Step 2: Verify backend services are running (direct access)
print_section "Step 2: Verify Backend Services (Direct Access)"

test_endpoint "Auth Service Health (Direct)" "http://localhost:8081/actuator/health"
AUTH_RUNNING=$?

test_endpoint "Catalog Service Health (Direct)" "http://localhost:8082/actuator/health"
CATALOG_RUNNING=$?

test_endpoint "Order Service Health (Direct)" "http://localhost:8083/actuator/health"
ORDER_RUNNING=$?

if [ $AUTH_RUNNING -ne 0 ] || [ $CATALOG_RUNNING -ne 0 ] || [ $ORDER_RUNNING -ne 0 ]; then
    print_warning "One or more backend services are not running"
    print_info "Please ensure all services are started:"
    [ $AUTH_RUNNING -ne 0 ] && print_info "  - Auth Service: cd auth-service && mvn spring-boot:run"
    [ $CATALOG_RUNNING -ne 0 ] && print_info "  - Catalog Service: cd catalog-service && mvn spring-boot:run"
    [ $ORDER_RUNNING -ne 0 ] && print_info "  - Order Service: cd order-service && mvn spring-boot:run"
fi

# Step 3: Verify Gateway routing to backend services
print_section "Step 3: Verify Gateway Routing to Backend Services"

print_info "Testing Gateway → Auth Service routing (Path: /api/auth/**)..."
test_endpoint "Gateway → Auth Service Health" "http://localhost:8080/api/auth/actuator/health"

print_info "Testing Gateway → Catalog Service routing (Path: /api/catalog/**)..."
test_endpoint "Gateway → Catalog Service Health" "http://localhost:8080/api/catalog/actuator/health"

print_info "Testing Gateway → Order Service routing (Path: /api/orders/**)..."
test_endpoint "Gateway → Order Service Health" "http://localhost:8080/api/orders/actuator/health"

# Step 4: Test authentication flow through gateway
print_section "Step 4: Test Authentication Flow Through Gateway"

RANDOM_EMAIL="testuser_${RANDOM}@example.com"
REGISTER_BODY='{"email":"'$RANDOM_EMAIL'","password":"password123","firstName":"Test","lastName":"User"}'

print_info "Attempting user registration through gateway..."

RESPONSE=$(curl -s -X POST "http://localhost:8080/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "$REGISTER_BODY" 2>/dev/null)

JWT_TOKEN=$(echo "$RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -n "$JWT_TOKEN" ]; then
    print_success "User registration successful through gateway"
    print_info "  JWT Token: ${JWT_TOKEN:0:50}..."
    ((PASSED_TESTS++))
else
    print_failure "User registration failed - no JWT token received"
    ((FAILED_TESTS++))
fi
((TOTAL_TESTS++))

# Step 5: Test catalog service access through gateway (with JWT)
print_section "Step 5: Test Catalog Service Access Through Gateway"

if [ -n "$JWT_TOKEN" ]; then
    print_info "Fetching products through gateway with JWT token..."
    
    PRODUCTS_RESPONSE=$(curl -s -w '\n%{http_code}' \
        -H "Authorization: Bearer $JWT_TOKEN" \
        "http://localhost:8080/api/catalog/products" 2>/dev/null)
    
    STATUS_CODE=$(echo "$PRODUCTS_RESPONSE" | tail -n1)
    
    if [ "$STATUS_CODE" = "200" ]; then
        print_success "Successfully retrieved products through gateway"
        ((PASSED_TESTS++))
    else
        print_failure "Failed to retrieve products - Status: $STATUS_CODE"
        ((FAILED_TESTS++))
    fi
    ((TOTAL_TESTS++))
else
    print_warning "Skipping catalog service test - no JWT token available"
fi

# Step 6: Test order service access through gateway (with JWT)
print_section "Step 6: Test Order Service Access Through Gateway"

if [ -n "$JWT_TOKEN" ]; then
    print_info "Fetching orders through gateway with JWT token..."
    
    ORDERS_RESPONSE=$(curl -s -w '\n%{http_code}' \
        -H "Authorization: Bearer $JWT_TOKEN" \
        "http://localhost:8080/api/orders" 2>/dev/null)
    
    STATUS_CODE=$(echo "$ORDERS_RESPONSE" | tail -n1)
    
    if [ "$STATUS_CODE" = "200" ]; then
        print_success "Successfully retrieved orders through gateway"
        ((PASSED_TESTS++))
    else
        print_failure "Failed to retrieve orders - Status: $STATUS_CODE"
        ((FAILED_TESTS++))
    fi
    ((TOTAL_TESTS++))
else
    print_warning "Skipping order service test - no JWT token available"
fi

# Step 7: Test CORS configuration
print_section "Step 7: Test CORS Configuration"

print_info "Testing CORS preflight request..."

CORS_RESPONSE=$(curl -s -i -X OPTIONS "http://localhost:8080/api/catalog/products" \
    -H "Origin: http://localhost:3000" \
    -H "Access-Control-Request-Method: GET" 2>/dev/null)

if echo "$CORS_RESPONSE" | grep -q "Access-Control-Allow-Origin"; then
    print_success "CORS headers present in response"
    CORS_ORIGIN=$(echo "$CORS_RESPONSE" | grep "Access-Control-Allow-Origin" | cut -d' ' -f2 | tr -d '\r')
    print_info "  Access-Control-Allow-Origin: $CORS_ORIGIN"
    ((PASSED_TESTS++))
else
    print_failure "CORS headers missing in response"
    ((FAILED_TESTS++))
fi
((TOTAL_TESTS++))

# Step 8: Test rate limiting (optional)
print_section "Step 8: Test Rate Limiting (Optional)"

print_info "Testing rate limiting by sending multiple requests..."
print_warning "Note: This test requires Redis to be running"

RATE_LIMIT_HIT=false

for i in {1..25}; do
    RESPONSE=$(curl -s -w '%{http_code}' -o /dev/null "http://localhost:8080/api/catalog/products" 2>/dev/null)
    
    if [ "$i" -eq 1 ]; then
        print_info "  Request $i : Success (Status: $RESPONSE)"
    fi
    
    if [ "$RESPONSE" = "429" ]; then
        print_success "Rate limiting is working - received 429 after $i requests"
        RATE_LIMIT_HIT=true
        break
    fi
    
    sleep 0.05
done

if [ "$RATE_LIMIT_HIT" = true ]; then
    print_success "Rate limiting test passed"
    ((PASSED_TESTS++))
else
    print_warning "Rate limiting not triggered (may need Redis or higher request volume)"
fi
((TOTAL_TESTS++))

# Final Summary
print_section "Verification Summary"

if [ $TOTAL_TESTS -gt 0 ]; then
    PASS_RATE=$(awk "BEGIN {printf \"%.2f\", ($PASSED_TESTS / $TOTAL_TESTS) * 100}")
else
    PASS_RATE=0
fi

echo -e "${CYAN}Total Tests: $TOTAL_TESTS${NC}"
echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
echo -e "${RED}Failed: $FAILED_TESTS${NC}"

if (( $(echo "$PASS_RATE >= 80" | bc -l) )); then
    echo -e "${GREEN}Pass Rate: $PASS_RATE%${NC}"
elif (( $(echo "$PASS_RATE >= 50" | bc -l) )); then
    echo -e "${YELLOW}Pass Rate: $PASS_RATE%${NC}"
else
    echo -e "${RED}Pass Rate: $PASS_RATE%${NC}"
fi

echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    print_success "All gateway routing tests passed!"
    print_info "The API Gateway can successfully reach all backend services."
    exit 0
elif (( $(echo "$PASS_RATE >= 70" | bc -l) )); then
    print_warning "Most tests passed, but some failures detected."
    print_info "Review the failures above and ensure all services are running correctly."
    exit 1
else
    print_failure "Multiple test failures detected."
    print_info "Please check:"
    print_info "  1. All services are running (gateway, auth, catalog, order)"
    print_info "  2. Config server is running and accessible"
    print_info "  3. Redis is running (for rate limiting)"
    print_info "  4. No port conflicts (8080, 8081, 8082, 8083)"
    exit 1
fi
