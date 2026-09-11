# Gateway Routing Verification Script
# Purpose: Verify that API Gateway can reach all backend services
# Author: E-Commerce Development Team
# Last Updated: August 2026

# Color output functions
function Write-Success {
    param([string]$Message)
    Write-Host "✓ $Message" -ForegroundColor Green
}

function Write-Failure {
    param([string]$Message)
    Write-Host "✗ $Message" -ForegroundColor Red
}

function Write-Info {
    param([string]$Message)
    Write-Host "ℹ $Message" -ForegroundColor Cyan
}

function Write-Warning {
    param([string]$Message)
    Write-Host "⚠ $Message" -ForegroundColor Yellow
}

function Write-SectionHeader {
    param([string]$Message)
    Write-Host "`n========================================" -ForegroundColor Magenta
    Write-Host "  $Message" -ForegroundColor Magenta
    Write-Host "========================================`n" -ForegroundColor Magenta
}

# Test results tracking
$script:TotalTests = 0
$script:PassedTests = 0
$script:FailedTests = 0

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Method = "GET",
        [hashtable]$Headers = @{},
        [string]$Body = $null,
        [int]$ExpectedStatus = 200
    )
    
    $script:TotalTests++
    
    try {
        Write-Host "Testing: $Name" -ForegroundColor Yellow
        Write-Host "  URL: $Url" -ForegroundColor Gray
        
        $params = @{
            Uri = $Url
            Method = $Method
            UseBasicParsing = $true
            TimeoutSec = 10
        }
        
        if ($Headers.Count -gt 0) {
            $params.Headers = $Headers
        }
        
        if ($Body) {
            $params.Body = $Body
            $params.ContentType = "application/json"
        }
        
        $response = Invoke-WebRequest @params -ErrorAction Stop
        
        if ($response.StatusCode -eq $ExpectedStatus) {
            Write-Success "$Name - Status: $($response.StatusCode)"
            $script:PassedTests++
            return $true
        } else {
            Write-Failure "$Name - Expected: $ExpectedStatus, Got: $($response.StatusCode)"
            $script:FailedTests++
            return $false
        }
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq $ExpectedStatus) {
            Write-Success "$Name - Status: $statusCode (Expected)"
            $script:PassedTests++
            return $true
        } else {
            Write-Failure "$Name - Error: $($_.Exception.Message)"
            if ($statusCode) {
                Write-Host "  Status Code: $statusCode" -ForegroundColor Gray
            }
            $script:FailedTests++
            return $false
        }
    }
}

# Main verification script
Write-SectionHeader "Gateway Routing Verification"
Write-Info "This script verifies that the API Gateway can reach all backend services"
Write-Info "Gateway URL: http://localhost:8080"
Write-Info "Date: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"

# Step 1: Verify Gateway is running
Write-SectionHeader "Step 1: Verify Gateway Service"
$gatewayRunning = Test-Endpoint -Name "Gateway Health Check" -Url "http://localhost:8080/actuator/health"

if (-not $gatewayRunning) {
    Write-Failure "Gateway service is not running on port 8080"
    Write-Warning "Please start the gateway service first: cd gateway-service && mvn spring-boot:run"
    exit 1
}

# Step 2: Verify backend services are running (direct access)
Write-SectionHeader "Step 2: Verify Backend Services (Direct Access)"

$authServiceRunning = Test-Endpoint -Name "Auth Service Health (Direct)" -Url "http://localhost:8081/actuator/health"
$catalogServiceRunning = Test-Endpoint -Name "Catalog Service Health (Direct)" -Url "http://localhost:8082/actuator/health"
$orderServiceRunning = Test-Endpoint -Name "Order Service Health (Direct)" -Url "http://localhost:8083/actuator/health"

if (-not ($authServiceRunning -and $catalogServiceRunning -and $orderServiceRunning)) {
    Write-Warning "One or more backend services are not running"
    Write-Info "Please ensure all services are started:"
    if (-not $authServiceRunning) { Write-Info "  - Auth Service: cd auth-service && mvn spring-boot:run" }
    if (-not $catalogServiceRunning) { Write-Info "  - Catalog Service: cd catalog-service && mvn spring-boot:run" }
    if (-not $orderServiceRunning) { Write-Info "  - Order Service: cd order-service && mvn spring-boot:run" }
}

# Step 3: Verify Gateway routing to backend services
Write-SectionHeader "Step 3: Verify Gateway Routing to Backend Services"

Write-Info "Testing Gateway → Auth Service routing (Path: /api/auth/**)..."
$authRouting = Test-Endpoint -Name "Gateway → Auth Service Health" -Url "http://localhost:8080/api/auth/actuator/health"

Write-Info "Testing Gateway → Catalog Service routing (Path: /api/catalog/**)..."
$catalogRouting = Test-Endpoint -Name "Gateway → Catalog Service Health" -Url "http://localhost:8080/api/catalog/actuator/health"

Write-Info "Testing Gateway → Order Service routing (Path: /api/orders/**)..."
$orderRouting = Test-Endpoint -Name "Gateway → Order Service Health" -Url "http://localhost:8080/api/orders/actuator/health"

# Step 4: Test authentication flow through gateway
Write-SectionHeader "Step 4: Test Authentication Flow Through Gateway"

$registerBody = @{
    email = "testuser_$(Get-Random)@example.com"
    password = "password123"
    firstName = "Test"
    lastName = "User"
} | ConvertTo-Json

Write-Info "Attempting user registration through gateway..."
$registrationSuccess = $false
$jwtToken = $null

try {
    $registerResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" `
        -Method POST `
        -ContentType "application/json" `
        -Body $registerBody `
        -ErrorAction Stop
    
    if ($registerResponse.token) {
        Write-Success "User registration successful through gateway"
        $jwtToken = $registerResponse.token
        $registrationSuccess = $true
        $script:PassedTests++
    } else {
        Write-Failure "Registration response missing JWT token"
        $script:FailedTests++
    }
    $script:TotalTests++
} catch {
    Write-Failure "User registration failed: $($_.Exception.Message)"
    $script:TotalTests++
    $script:FailedTests++
}

# Step 5: Test catalog service access through gateway (with JWT)
Write-SectionHeader "Step 5: Test Catalog Service Access Through Gateway"

if ($jwtToken) {
    $headers = @{
        Authorization = "Bearer $jwtToken"
    }
    
    Write-Info "Fetching products through gateway with JWT token..."
    try {
        $productsResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/catalog/products" `
            -Method GET `
            -Headers $headers `
            -ErrorAction Stop
        
        Write-Success "Successfully retrieved products through gateway"
        Write-Info "  Products count: $($productsResponse.Count)"
        $script:PassedTests++
        $script:TotalTests++
    } catch {
        Write-Failure "Failed to retrieve products: $($_.Exception.Message)"
        $script:FailedTests++
        $script:TotalTests++
    }
} else {
    Write-Warning "Skipping catalog service test - no JWT token available"
}

# Step 6: Test order service access through gateway (with JWT)
Write-SectionHeader "Step 6: Test Order Service Access Through Gateway"

if ($jwtToken) {
    $headers = @{
        Authorization = "Bearer $jwtToken"
    }
    
    Write-Info "Fetching orders through gateway with JWT token..."
    try {
        $ordersResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/orders" `
            -Method GET `
            -Headers $headers `
            -ErrorAction Stop
        
        Write-Success "Successfully retrieved orders through gateway"
        Write-Info "  Orders count: $($ordersResponse.Count)"
        $script:PassedTests++
        $script:TotalTests++
    } catch {
        Write-Failure "Failed to retrieve orders: $($_.Exception.Message)"
        $script:FailedTests++
        $script:TotalTests++
    }
} else {
    Write-Warning "Skipping order service test - no JWT token available"
}

# Step 7: Test CORS configuration
Write-SectionHeader "Step 7: Test CORS Configuration"

Write-Info "Testing CORS preflight request..."
try {
    $corsResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/catalog/products" `
        -Method OPTIONS `
        -Headers @{
            "Origin" = "http://localhost:3000"
            "Access-Control-Request-Method" = "GET"
        } `
        -UseBasicParsing `
        -ErrorAction Stop
    
    $corsHeaders = $corsResponse.Headers
    
    if ($corsHeaders["Access-Control-Allow-Origin"]) {
        Write-Success "CORS headers present in response"
        Write-Info "  Access-Control-Allow-Origin: $($corsHeaders['Access-Control-Allow-Origin'])"
        $script:PassedTests++
    } else {
        Write-Failure "CORS headers missing in response"
        $script:FailedTests++
    }
    $script:TotalTests++
} catch {
    Write-Failure "CORS preflight request failed: $($_.Exception.Message)"
    $script:FailedTests++
    $script:TotalTests++
}

# Step 8: Test rate limiting (optional)
Write-SectionHeader "Step 8: Test Rate Limiting (Optional)"

Write-Info "Testing rate limiting by sending multiple requests..."
Write-Warning "Note: This test requires Redis to be running"

$rateLimitHit = $false
for ($i = 1; $i -le 25; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/catalog/products" `
            -Method GET `
            -UseBasicParsing `
            -ErrorAction Stop `
            -TimeoutSec 5
        
        if ($i -eq 1) {
            Write-Info "  Request $i : Success (Status: $($response.StatusCode))"
        }
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq 429) {
            Write-Success "Rate limiting is working - received 429 after $i requests"
            $rateLimitHit = $true
            break
        }
    }
    Start-Sleep -Milliseconds 50
}

if ($rateLimitHit) {
    Write-Success "Rate limiting test passed"
    $script:PassedTests++
} else {
    Write-Warning "Rate limiting not triggered (may need Redis or higher request volume)"
}
$script:TotalTests++

# Final Summary
Write-SectionHeader "Verification Summary"

$passRate = if ($script:TotalTests -gt 0) { [math]::Round(($script:PassedTests / $script:TotalTests) * 100, 2) } else { 0 }

Write-Host "Total Tests: $script:TotalTests" -ForegroundColor Cyan
Write-Host "Passed: $script:PassedTests" -ForegroundColor Green
Write-Host "Failed: $script:FailedTests" -ForegroundColor Red
Write-Host "Pass Rate: $passRate%" -ForegroundColor $(if ($passRate -ge 80) { "Green" } elseif ($passRate -ge 50) { "Yellow" } else { "Red" })

Write-Host "`n" -NoNewline

if ($script:FailedTests -eq 0) {
    Write-Success "All gateway routing tests passed!"
    Write-Info "The API Gateway can successfully reach all backend services."
    exit 0
} elseif ($passRate -ge 70) {
    Write-Warning "Most tests passed, but some failures detected."
    Write-Info "Review the failures above and ensure all services are running correctly."
    exit 1
} else {
    Write-Failure "Multiple test failures detected."
    Write-Info "Please check:"
    Write-Info "  1. All services are running (gateway, auth, catalog, order)"
    Write-Info "  2. Config server is running and accessible"
    Write-Info "  3. Redis is running (for rate limiting)"
    Write-Info "  4. No port conflicts (8080, 8081, 8082, 8083)"
    exit 1
}
