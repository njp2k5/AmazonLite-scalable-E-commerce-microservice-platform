# JWT Authentication System - Documentation

## Overview

This document explains the JWT authentication system implemented in the Product Service microservice.

---

## Architecture & Components

### 1. **User Entity** (`User.java`)
Represents a user in the database with the following fields:

```
📦 User Table
├── id: Long (Primary Key, Auto-generated)
├── email: String (Unique, Required)
├── password_hash: String (BCrypt hashed, Required)
├── role: Enum (ADMIN, SELLER, CUSTOMER)
└── created_at: LocalDateTime (Auto-set on creation)
```

**User Roles (RBAC):**
- `ADMIN` - Full system access, can manage all resources
- `SELLER` - Can manage products and orders
- `CUSTOMER` - Can browse products and place orders

---

### 2. **JWT (JSON Web Token) Utility** (`JwtUtil.java`)

**What is JWT?**
JWT is a stateless token format used for authentication in modern APIs. It contains:
- **Header**: Token type and hashing algorithm
- **Payload**: Claims (user data like ID, email, role)
- **Signature**: Cryptographic signature to verify token authenticity

**Key Methods:**
```java
// Generate a token for a user
generateToken(userId, email, role) → "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

// Validate token signature and expiration
validateToken(token) → true/false

// Extract user info from token
extractEmail(token) → "user@example.com"
extractUserId(token) → 1
extractRole(token) → "CUSTOMER"
```

**Token Signing:**
- Algorithm: HMAC SHA-256 (HS256)
- Secret key: Loaded from `application.yml` (`jwt.secret`)
- Expiration: 24 hours

---

### 3. **Authentication Service** (`AuthService.java`)

**Registration Process:**
```
User Input (email, password, role)
              ↓
  Check if email exists? 
              ↓
  Hash password using BCrypt
              ↓
  Save User to Database
              ↓
  Generate JWT Token
              ↓
  Return Token + User Info
```

**Login Process:**
```
User Input (email, password)
              ↓
  Find user by email
              ↓
  Verify password with BCrypt
              ↓
  Generate JWT Token
              ↓
  Return Token + User Info
```

**Key Security Features:**
- `BCryptPasswordEncoder`: Passwords are hashed with salt before storage
- Never stores plain-text passwords
- Safe password comparison using `BCrypt.matches()`

---

### 4. **Authentication Controller** (`AuthController.java`)

Two main endpoints:

#### **POST /auth/register**
Register a new user and get JWT token.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "securepassword123",
  "role": "CUSTOMER"   // Options: CUSTOMER, SELLER, ADMIN
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwidXNlcklkIjoxLCJyb2xlIjoiQ1VTVE9NRVIifQ.abc123...",
  "userId": 1,
  "email": "user@example.com",
  "role": "CUSTOMER",
  "message": "User registered successfully"
}
```

#### **POST /auth/login**
Login with email and password to get JWT token.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "securepassword123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "user@example.com",
  "role": "CUSTOMER",
  "message": "Login successful"
}
```

---

## Database Connection

**Database:** Neon PostgreSQL (Cloud-hosted)
**Connection String:** Configured in `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://ep-crimson-hat-appwfmvz-pooler.c-7.us-east-1.aws.neon.tech:5432/neondb
    username: neondb_owner
    password: [YOUR_PASSWORD]
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update  # Auto-creates/updates tables
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

---

## Test Users (Pre-loaded on Startup)

The application automatically creates 3 test users on first startup:

### Admin User
```
Email: admin@amazonlite.com
Password: admin123
Role: ADMIN
```

### Seller User
```
Email: seller@amazonlite.com
Password: seller123
Role: SELLER
```

### Customer User
```
Email: customer@amazonlite.com
Password: customer123
Role: CUSTOMER
```

---

## Testing the Authentication System

### Prerequisites
- Service running on `http://localhost:8081`
- Postman or cURL installed

### Test 1: Register a New User

**Command (PowerShell):**
```powershell
$body = @{
    email = "john.doe@amazonlite.com"
    password = "john123456"
    role = "CUSTOMER"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8081/auth/register" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 4,
  "email": "john.doe@amazonlite.com",
  "role": "CUSTOMER",
  "message": "User registered successfully"
}
```

---

### Test 2: Login with Existing User

**Command (PowerShell):**
```powershell
$body = @{
    email = "admin@amazonlite.com"
    password = "admin123"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8081/auth/login" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body

$response.Content | ConvertFrom-Json | ConvertTo-Json -Depth 4
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "admin@amazonlite.com",
  "role": "ADMIN",
  "message": "Login successful"
}
```

---

### Test 3: Verify Token Contains User Info

**PowerShell Script to Decode JWT:**
```powershell
function Decode-JwtToken($token) {
    $parts = $token.Split('.')
    if ($parts.Count -ne 3) {
        Write-Error "Invalid token format"
        return
    }
    
    # Add padding if needed
    $payload = $parts[1]
    while ($payload.Length % 4) {
        $payload += "="
    }
    
    # Decode base64
    $decoded = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($payload))
    $decoded | ConvertFrom-Json | ConvertTo-Json -Depth 4
}

# Example token from login response
$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
Decode-JwtToken -token $token
```

**Token Payload Example:**
```json
{
  "sub": "admin@amazonlite.com",
  "userId": 1,
  "role": "ADMIN",
  "iat": 1714886400,
  "exp": 1714972800
}
```

---

### Test 4: Error Cases

#### **4a. Register with Duplicate Email**
```powershell
$body = @{
    email = "admin@amazonlite.com"  # Email already exists
    password = "test123"
    role = "CUSTOMER"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8081/auth/register" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body
```

**Expected Response (409 Conflict):**
```json
{
  "message": "Email already registered: admin@amazonlite.com"
}
```

---

#### **4b. Login with Invalid Password**
```powershell
$body = @{
    email = "admin@amazonlite.com"
    password = "wrongpassword"  # Incorrect password
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8081/auth/login" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body
```

**Expected Response (401 Unauthorized):**
```json
{
  "message": "Invalid credentials: Invalid password for user: admin@amazonlite.com"
}
```

---

#### **4c. Login with Non-existent Email**
```powershell
$body = @{
    email = "nonexistent@amazonlite.com"
    password = "test123"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8081/auth/login" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body
```

**Expected Response (401 Unauthorized):**
```json
{
  "message": "Invalid credentials: User not found: nonexistent@amazonlite.com"
}
```

---

## Role-Based Access Control (RBAC)

The `AuthService.verifyRole()` method can be used in other controllers to check user roles:

```java
@GetMapping("/admin/reports")
public ResponseEntity<?> getAdminReports(
        @RequestHeader("Authorization") String authHeader) {
    
    String token = authHeader.replace("Bearer ", "");
    
    if (!authService.verifyRole(token, "ADMIN")) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Access denied. Admin role required.");
    }
    
    // Generate admin reports...
    return ResponseEntity.ok("Admin data...");
}
```

---

## Security Best Practices Applied

✅ **Password Hashing**: BCrypt with automatic salt generation
✅ **Token Signing**: HMAC SHA-256 signature prevents tampering
✅ **Token Expiration**: 24-hour expiration reduces exposure window
✅ **Role-Based Access**: RBAC support for different user types
✅ **Email Uniqueness**: Enforced unique emails in database
✅ **Stateless Authentication**: JWT tokens don't require server-side sessions

---

## Troubleshooting

### Issue: "Database connection refused"
**Solution:** Verify Neon PostgreSQL credentials in `application.yml`

### Issue: "User already exists"
**Solution:** Register with a different email address

### Issue: "Invalid token in protected endpoints"
**Solution:** Ensure token is passed in `Authorization: Bearer <token>` header

### Issue: "Table 'users' does not exist"
**Solution:** Restart the application to trigger Hibernate's `ddl-auto: update`

---

## Next Steps

1. **JWT Validation Interceptor**: Add a filter to validate JWT tokens in all protected endpoints
2. **Refresh Token**: Implement refresh token for longer sessions
3. **Email Verification**: Add email confirmation before account activation
4. **Two-Factor Authentication**: Add 2FA for enhanced security
5. **Rate Limiting**: Implement rate limiting on auth endpoints to prevent brute-force attacks

---

**Created**: May 6, 2026
**Version**: 1.0
**Author**: Authentication System Implementation
