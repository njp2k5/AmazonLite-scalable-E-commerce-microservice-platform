# JWT Authentication - Quick Testing Guide

## 🚀 Start Service

```powershell
cd d:\my_codes\FAANG-projects\AmazonLite\services\product-service
java -jar target/product-service-1.0.0.jar
```

**Expected Output:**
```
Started ProductApplication
==================== Initializing Test Users ====================
✓ Admin created:
  Email: admin@amazonlite.com
  Password: admin123
  
✓ Seller created:
  Email: seller@amazonlite.com
  Password: seller123
  
✓ Customer created:
  Email: customer@amazonlite.com
  Password: customer123
```

---

## 📝 Test 1: Login as Admin

**Command:**
```powershell
$body = @{
    email = "admin@amazonlite.com"
    password = "admin123"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8081/auth/login" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body -UseBasicParsing

$response.Content | ConvertFrom-Json | ConvertTo-Json
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBhbWF6b25saXRlLmNvbSIsInVzZXJJZCI6MSwicm9sZSI6IkFETUlOIiwiaWF0IjoxNzc4MDkxMTU5LCJleHAiOjE3NzgxNzc1NTl9.dChtbwlg6l94qLqXeuy0q3yFaQ7VrT31WG5pOmxskO4",
  "userId": 1,
  "email": "admin@amazonlite.com",
  "role": "ADMIN",
  "message": "Login successful"
}
```

---

## 📝 Test 2: Login as Seller

**Command:**
```powershell
$body = @{
    email = "seller@amazonlite.com"
    password = "seller123"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8081/auth/login" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body -UseBasicParsing

$response.Content | ConvertFrom-Json | ConvertTo-Json
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 2,
  "email": "seller@amazonlite.com",
  "role": "SELLER",
  "message": "Login successful"
}
```

---

## 📝 Test 3: Login as Customer

**Command:**
```powershell
$body = @{
    email = "customer@amazonlite.com"
    password = "customer123"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8081/auth/login" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body -UseBasicParsing

$response.Content | ConvertFrom-Json | ConvertTo-Json
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 3,
  "email": "customer@amazonlite.com",
  "role": "CUSTOMER",
  "message": "Login successful"
}
```

---

## 📝 Test 4: Register New User

**Command:**
```powershell
$body = @{
    email = "newuser@amazonlite.com"
    password = "securepassword123"
    role = "CUSTOMER"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8081/auth/register" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body -UseBasicParsing

$response.Content | ConvertFrom-Json | ConvertTo-Json
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 4,
  "email": "newuser@amazonlite.com",
  "role": "CUSTOMER",
  "message": "User registered successfully"
}
```

---

## ❌ Test 5: Register with Existing Email (Error Case)

**Command:**
```powershell
$body = @{
    email = "admin@amazonlite.com"  # Already registered!
    password = "test123"
    role = "CUSTOMER"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/auth/register" `
      -Method POST `
      -Headers @{"Content-Type"="application/json"} `
      -Body $body -UseBasicParsing
} catch {
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)"
    Write-Host $_.Exception.Response.StatusCode
}
```

**Expected Response (409):**
```
Status: 409 Conflict
Response: {"token":null,"userId":null,"email":null,"role":null,"message":"Email already registered: admin@amazonlite.com"}
```

---

## ❌ Test 6: Login with Wrong Password (Error Case)

**Command:**
```powershell
$body = @{
    email = "admin@amazonlite.com"
    password = "wrongpassword"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/auth/login" `
      -Method POST `
      -Headers @{"Content-Type"="application/json"} `
      -Body $body -UseBasicParsing
} catch {
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)"
    Write-Host $_.Exception.Response.GetResponseStream() | Get-Content
}
```

**Expected Response (401):**
```
Status: 401 Unauthorized
Response: {"token":null,"userId":null,"email":null,"role":null,"message":"Invalid credentials: Invalid password for user: admin@amazonlite.com"}
```

---

## ❌ Test 7: Login with Non-existent Email

**Command:**
```powershell
$body = @{
    email = "nonexistent@amazonlite.com"
    password = "test123"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/auth/login" `
      -Method POST `
      -Headers @{"Content-Type"="application/json"} `
      -Body $body -UseBasicParsing
} catch {
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)"
}
```

**Expected Response (401):**
```
Status: 401 Unauthorized
```

---

## 🔍 Test 8: Decode JWT Token (Inspect Claims)

**PowerShell Function to Decode JWT:**

```powershell
function Decode-JwtToken {
    param([string]$token)
    
    $parts = $token.Split('.')
    if ($parts.Count -ne 3) {
        Write-Error "Invalid token format"
        return
    }
    
    # Decode payload (part 2)
    $payload = $parts[1]
    
    # Add padding if needed
    while ($payload.Length % 4) {
        $payload += "="
    }
    
    # Decode from base64
    $decoded = [System.Text.Encoding]::UTF8.GetString(
        [System.Convert]::FromBase64String($payload)
    )
    
    $decoded | ConvertFrom-Json | ConvertTo-Json -Depth 4
}

# Usage:
$token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBhbWF6b25saXRlLmNvbSIsInVzZXJJZCI6MSwicm9sZSI6IkFETUlOIiwiaWF0IjoxNzc4MDkxMTU5LCJleHAiOjE3NzgxNzc1NTl9.dChtbwlg6l94qLqXeuy0q3yFaQ7VrT31WG5pOmxskO4"
Decode-JwtToken -token $token
```

**Expected Output:**
```json
{
  "sub": "admin@amazonlite.com",
  "userId": 1,
  "role": "ADMIN",
  "iat": 1778091159,
  "exp": 1778177559
}
```

**Explanation:**
- `sub`: Subject (email)
- `userId`: User ID from database
- `role`: User's role (ADMIN/SELLER/CUSTOMER)
- `iat`: Issued at (Unix timestamp)
- `exp`: Expiration time (Unix timestamp)

**Check Token Expiration:**
```powershell
$payload = Decode-JwtToken -token $token | ConvertFrom-Json
$expDate = (Get-Date 01.01.1970) + ([System.TimeSpan]::fromseconds($payload.exp))
Write-Host "Token expires at: $expDate"
```

---

## ✅ Test 9: Health Check

**Command:**
```powershell
Invoke-WebRequest -Uri "http://localhost:8081/auth/health" `
  -UseBasicParsing | Select-Object -ExpandProperty Content
```

**Expected Response:**
```json
{"status":"Auth service is healthy"}
```

---

## 📊 Test Summary

| Test | Endpoint | Method | Status | Purpose |
|------|----------|--------|--------|---------|
| 1 | `/auth/login` | POST | 200 | Admin login |
| 2 | `/auth/login` | POST | 200 | Seller login |
| 3 | `/auth/login` | POST | 200 | Customer login |
| 4 | `/auth/register` | POST | 201 | New user registration |
| 5 | `/auth/register` | POST | 409 | Duplicate email error |
| 6 | `/auth/login` | POST | 401 | Wrong password error |
| 7 | `/auth/login` | POST | 401 | Non-existent user error |
| 8 | JWT Decode | - | - | Inspect token claims |
| 9 | `/auth/health` | GET | 200 | Service health check |

---

## 🛠️ Advanced Testing: Using Token in Protected Requests

**Step 1: Get Token**
```powershell
$loginBody = @{
    email = "admin@amazonlite.com"
    password = "admin123"
} | ConvertTo-Json

$loginResponse = Invoke-WebRequest -Uri "http://localhost:8081/auth/login" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body $loginBody -UseBasicParsing

$token = ($loginResponse.Content | ConvertFrom-Json).token
Write-Host "Token: $token"
```

**Step 2: Use Token in Protected Request**
```powershell
# Example: Get products with authorization
$response = Invoke-WebRequest -Uri "http://localhost:8081/products" `
  -Headers @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
  } -UseBasicParsing

$response.Content | ConvertFrom-Json | ConvertTo-Json
```

---

## 📱 Testing with Postman

### 1. Register Endpoint

**Method:** POST
**URL:** `http://localhost:8081/auth/register`
**Headers:**
```
Content-Type: application/json
```
**Body (JSON):**
```json
{
  "email": "testuser@example.com",
  "password": "testpass123",
  "role": "CUSTOMER"
}
```

### 2. Login Endpoint

**Method:** POST
**URL:** `http://localhost:8081/auth/login`
**Headers:**
```
Content-Type: application/json
```
**Body (JSON):**
```json
{
  "email": "admin@amazonlite.com",
  "password": "admin123"
}
```

### 3. Protected Request

**Method:** GET
**URL:** `http://localhost:8081/products`
**Headers:**
```
Authorization: Bearer <your-jwt-token>
Content-Type: application/json
```

---

## ⚠️ Common Issues & Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| 401 Unauthorized | Wrong password | Check password is correct |
| 409 Conflict | Email already exists | Use different email |
| 400 Bad Request | Invalid JSON format | Check JSON syntax |
| Cannot connect | Service not running | Start with `java -jar ...` |
| Token expired | Token > 24 hours old | Login again to get new token |
| 404 Not Found | Wrong endpoint URL | Check URL spelling |

---

## 🎯 Testing Checklist

- [ ] Service starts with test data initialized
- [ ] Login endpoint returns valid JWT token
- [ ] Register endpoint creates new user
- [ ] Duplicate email prevented (409)
- [ ] Invalid password rejected (401)
- [ ] JWT token contains correct claims
- [ ] Token has 24-hour expiration
- [ ] Password is properly hashed (BCrypt)
- [ ] Health endpoint responds (200)
- [ ] Database connection established

---

**Test Guide Version**: 1.0
**Created**: May 6, 2026
**Last Updated**: May 6, 2026

