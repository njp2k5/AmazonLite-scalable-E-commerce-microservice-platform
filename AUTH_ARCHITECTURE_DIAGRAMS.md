# JWT Authentication System - Architecture & Flow Diagrams

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT APPLICATION                          │
│                    (Web/Mobile/Desktop App)                         │
└────────────────────────┬────────────────────────────────────────────┘
                         │
                    API Requests
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
    ┌────────┐      ┌────────┐      ┌────────┐
    │Register│      │ Login  │      │Protected
    │Request │      │Request │      │Endpoints
    └────────┘      └────────┘      └────────┘
        │                │                │
        └────────────────┼────────────────┘
                         │
                 ▼──────────────────────▼
        ┌──────────────────────────────────────┐
        │   PRODUCT SERVICE (Spring Boot)      │
        │  ┌────────────────────────────────┐  │
        │  │  AuthController                │  │
        │  │  • POST /auth/register         │  │
        │  │  • POST /auth/login            │  │
        │  │  • GET /auth/health            │  │
        │  └────────────────────────────────┘  │
        │                                      │
        │  ┌────────────────────────────────┐  │
        │  │  AuthService                   │  │
        │  │  • register(email, pwd, role)  │  │
        │  │  • login(email, password)      │  │
        │  │  • verifyRole(token, role)     │  │
        │  └────────────────────────────────┘  │
        │                                      │
        │  ┌────────────────────────────────┐  │
        │  │  JwtUtil                       │  │
        │  │  • generateToken()             │  │
        │  │  • validateToken()             │  │
        │  │  • extractClaims()             │  │
        │  └────────────────────────────────┘  │
        │                                      │
        │  ┌────────────────────────────────┐  │
        │  │  Spring Security Config        │  │
        │  │  • Enable JPA Repositories     │  │
        │  │  • Disable CSRF (JWT used)     │  │
        │  │  • Allow /auth/** public       │  │
        │  └────────────────────────────────┘  │
        └──────────────────┬───────────────────┘
                           │
                 ┌─────────┴─────────┐
                 │                   │
        ┌────────▼────────┐  ┌──────▼────────┐
        │  UserRepository │  │  DataInitial- │
        │  (JPA)          │  │  izer         │
        │  findByEmail()  │  │  (Startup)    │
        │  save()         │  └────────────────┘
        │  findAll()      │
        └────────┬────────┘
                 │
                 ▼
        ┌──────────────────────────────────────┐
        │   PostgreSQL (Neon Cloud)            │
        │  ┌────────────────────────────────┐  │
        │  │  users TABLE                   │  │
        │  │  ├── id (PK)                   │  │
        │  │  ├── email (UNIQUE)            │  │
        │  │  ├── password_hash (BCrypt)    │  │
        │  │  ├── role (ENUM)               │  │
        │  │  └── created_at                │  │
        │  └────────────────────────────────┘  │
        │                                      │
        │  Connection:                         │
        │  Host: ep-crimson-hat...             │
        │  Port: 5432                          │
        │  Database: neondb                    │
        │  SSL: REQUIRED                       │
        └──────────────────────────────────────┘
```

---

## 📋 Registration Flow Diagram

```
User Submits Registration
    │
    │ POST /auth/register
    │ {email, password, role}
    │
    ▼
AuthController.register()
    │
    ├─► Validate Input
    │   └─► Email format? ✓
    │   └─► Password length ≥ 6? ✓
    │   └─► Role valid? ✓
    │
    ├─► AuthService.register(request)
    │   │
    │   ├─► Check existingUser = userRepository.findByEmail(email)
    │   │   └─► If exists: ❌ Throw IllegalArgumentException (409 Conflict)
    │   │
    │   ├─► Hash password: hashedPassword = BCrypt.encode(password)
    │   │   └─► Automatic salt generation
    │   │   └─► Cost factor: 10 (default)
    │   │
    │   ├─► Create User entity
    │   │   ├─► user.email = email
    │   │   ├─► user.passwordHash = hashedPassword
    │   │   ├─► user.role = CUSTOMER/SELLER/ADMIN
    │   │   └─► user.createdAt = NOW()
    │   │
    │   ├─► Save user = userRepository.save(user)
    │   │   └─► INSERT INTO users ...
    │   │   └─► Database generates: id, timestamp
    │   │
    │   ├─► Generate JWT token
    │   │   └─► jwtUtil.generateToken(id, email, role)
    │   │   └─► Token expires in 24 hours
    │   │
    │   └─► Return LoginResponse
    │       ├─► token
    │       ├─► userId
    │       ├─► email
    │       ├─► role
    │       └─► message: "User registered successfully"
    │
    ▼
Return 201 Created + JWT Token to Client
```

---

## 🔓 Login Flow Diagram

```
User Submits Login Credentials
    │
    │ POST /auth/login
    │ {email, password}
    │
    ▼
AuthController.login()
    │
    ├─► Validate Input
    │   └─► Email format? ✓
    │   └─► Password not empty? ✓
    │
    ├─► AuthService.login(request)
    │   │
    │   ├─► Find user: userOpt = userRepository.findByEmail(email)
    │   │   └─► If not found: ❌ Throw IllegalArgumentException (401)
    │   │
    │   ├─► Verify Password
    │   │   └─► matches = BCrypt.matches(plainPassword, storedHash)
    │   │   └─► Compares without exposing hash
    │   │   └─► If false: ❌ Throw IllegalArgumentException (401)
    │   │
    │   ├─► Token generation: jwtUtil.generateToken(userId, email, role)
    │   │   │
    │   │   ├─► Create token parts:
    │   │   │   ┌─────────────────────────────┐
    │   │   │   │ HEADER (Algorithm)          │
    │   │   │   │ {                           │
    │   │   │   │   "alg": "HS256",           │
    │   │   │   │   "typ": "JWT"              │
    │   │   │   │ }                           │
    │   │   │   └─────────────────────────────┘
    │   │   │           │
    │   │   │   Base64URL Encode
    │   │   │           │
    │   │   │   ┌───────▼──────────────────────┐
    │   │   │   │ eyJhbGciOiJIUzI1NiJ9         │
    │   │   │   └───────┬──────────────────────┘
    │   │   │           │
    │   │   ├─► Create Payload (Claims):
    │   │   │   ┌─────────────────────────────┐
    │   │   │   │ PAYLOAD (User Data)         │
    │   │   │   │ {                           │
    │   │   │   │   "sub": "email@....",      │
    │   │   │   │   "userId": 1,              │
    │   │   │   │   "role": "ADMIN",          │
    │   │   │   │   "iat": 1778091159,        │
    │   │   │   │   "exp": 1778177559         │
    │   │   │   │ }                           │
    │   │   │   └─────────────────────────────┘
    │   │   │           │
    │   │   │   Base64URL Encode
    │   │   │           │
    │   │   │   ┌───────▼──────────────────────┐
    │   │   │   │ eyJzdWIiOiJhZG1i...          │
    │   │   │   └───────┬──────────────────────┘
    │   │   │           │
    │   │   ├─► Sign with secret:
    │   │   │   ┌─────────────────────────────────────┐
    │   │   │   │ SIGNATURE = HMACSHA256(             │
    │   │   │   │   header.payload,                   │
    │   │   │   │   "jwt-secret-key-from-yaml"       │
    │   │   │   │ )                                   │
    │   │   │   └─────────────────────────────────────┘
    │   │   │           │
    │   │   │   Base64URL Encode
    │   │   │           │
    │   │   │   ┌───────▼──────────────────────┐
    │   │   │   │ dChtbwlg6l94qLqXeuy0q3y...   │
    │   │   │   └───────┬──────────────────────┘
    │   │   │
    │   │   └─► COMBINE PARTS
    │   │       header.payload.signature
    │   │
    │   └─► Return LoginResponse
    │       ├─► token
    │       ├─► userId
    │       ├─► email
    │       ├─► role
    │       └─► message: "Login successful"
    │
    ▼
Return 200 OK + JWT Token to Client
```

---

## 🔒 Password Hashing (BCrypt) Explanation

### What is BCrypt?
BCrypt is an adaptive password hashing algorithm that automatically handles:
- **Salt generation**: Random data preventing rainbow table attacks
- **Iterations**: Adjustable "cost factor" making brute-force harder

### BCrypt Process

```
Plain Password Input: "admin123"
    │
    ▼
┌──────────────────────────────┐
│ BCryptPasswordEncoder        │
│                              │
│ 1. Generate random salt      │
│    └─ 2^10 iterations        │
│                              │
│ 2. Hash password with salt   │
│    └─ Multiple rounds        │
│                              │
│ 3. Store: salt + hash        │
└──────────────────────────────┘
    │
    ▼
Stored Hash (in database):
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36mB6DG

Format: $2a$ = BCrypt
        $10$ = Cost factor (2^10 = 1024 iterations)
        N9qo8uLOickgx2... = Salt + Hash

---

Later, during login:

User enters password: "admin123"
    │
    ▼
┌–─────────────────────────────────┐
│ BCrypt.matches(userInput, hash)  │
│                                  │
│ 1. Extract salt from stored hash │
│ 2. Hash userInput with that salt │
│ 3. Compare results               │
└──────────────────────────────────┘
    │
    ├─ ✓ MATCH    → Login successful
    │
    └─ ✗ NO MATCH → Login failed (401)
```

**Key Benefits:**
- ✅ Impossible to reverse hash to get password
- ✅ Salt prevents rainbow table attacks
- ✅ Adaptive iterations slow down brute-force attempts
- ✅ Cost factor can increase as computers get faster

---

## 🎫 JWT Token Verification Flow

```
Browser/Client receives JWT Token
    │
    │ Token is stored (localStorage/cookie)
    │
    ├──────────────────────────────────┐
    │                                  │
    │ On Protected Resource Request   │
    │                                  │
    ▼                                  │
Authorization: Bearer <token>         │
    │                                  │
    │ Send with request header         │
    │                                  │
    ▼                                  │
AuthController receives request
    │
    ├─► Extract token from header
    │   └─ Remove "Bearer " prefix
    │
    ├─► Call JwtUtil.validateToken(token)
    │   │
    │   ├─► Parse JWT parts:
    │   │   └─────────────────────────────
    │   │   │ Header . Payload . Signature │
    │   │   └─────────────────────────────
    │   │
    │   ├─► Verify Signature:
    │   │   ├─► Extract stored signature
    │   │   ├─► Recalculate: HMACSHA256(header.payload, secret)
    │   │   └─► Compare: stored == recalculated?
    │   │
    │   ├─► Check Expiration:
    │   │   ├─► Extract exp claim
    │   │   └─► NOW() < exp?
    │   │
    │   └─► Return: ✓ VALID or ✗ INVALID
    │
    ├─► If VALID:
    │   ├─► Extract claims: userId, email, role
    │   ├─► Grant access to resource
    │   └─► Process request
    │
    └─► If INVALID or EXPIRED:
        ├─► Return 401 Unauthorized
        └─► Request user to login again
```

---

## 🎯 RBAC (Role-Based Access Control) Structure

```
┌────────────────────────────────────────────────────┐
│                  USER ROLES                        │
└────────────────────────────────────────────────────┘

┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│    ADMIN    │         │    SELLER    │         │   CUSTOMER   │
└─────────────┘         └──────────────┘         └──────────────┘
      │                        │                        │
      │                        │                        │
      ├─ Manage users          ├─ Manage products       ├─ Browse products
      ├─ View reports          ├─ Manage orders         ├─ Place orders
      ├─ System settings       ├─ View inventory        ├─ Track orders
      ├─ Audit logs            ├─ Analytics             ├─ User profile
      ├─ Manage roles          ├─ Customer support      └─ Review products
      └─ Full access           └─ View sales reports

---

Implementation in Code:

@GetMapping("/admin/reports")
public ResponseEntity<?> getAdminReports(
        @RequestHeader("Authorization") String authHeader) {
    
    String token = authHeader.replace("Bearer ", "");
    
    // Extract role from JWT token
    String userRole = jwtUtil.extractRole(token);  // "ADMIN"
    
    // Verify role matches requirement
    if (!userRole.equals("ADMIN")) {
        return ResponseEntity.status(403)
            .body("Forbidden: Admin access required");
    }
    
    // Return admin data
    return ResponseEntity.ok("Admin Reports...");
}
```

---

## 📊 Data Flow: Complete User Journey

```
┌─────────────────────────────────────────────────────────────────┐
│                    NEW USER REGISTRATION                        │
└─────────────────────────────────────────────────────────────────┘

1. User Action
   └─ Fill registration form: email, password, role

2. Form Submission
   └─ POST /auth/register
      Payload: {email, password, role}

3. Server Processing
   └─ 3a. Validate inputs (email format, password length)
   └─ 3b. Check if email exists in database
          └─ If YES: Return 409 Conflict
   └─ 3c. Hash password with BCrypt
   └─ 3d. Insert new user record
          INSERT INTO users (email, password_hash, role, created_at)
          VALUES ('user@example.com', '$2a$10$...', 'CUSTOMER', NOW())
   └─ 3e. Assign auto-generated ID from database
   └─ 3f. Create JWT token
   └─ 3g. Return token to client

4. Client Receives Response
   Response: 201 Created
   {
     "token": "eyJhbGciOiJIUzI1NiJ9...",
     "userId": 4,
     "email": "user@example.com",
     "role": "CUSTOMER",
     "message": "User registered successfully"
   }

5. Client Storage
   └─ Save token in localStorage
      localStorage.setItem("authToken", token);

6. Subsequent Requests
   └─ Every API call includes token
      Headers: {
        "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9...",
        "Content-Type": "application/json"
      }

7. Server Verification
   └─ 7a. Extract token from Authorization header
   └─ 7b. Validate signature with secret key
   └─ 7c. Check expiration (must be < 24 hours)
   └─ 7d. Extract user claims (userId, email, role)
   └─ 7e. Check if user has permission for resource
   └─ 7f. Allow/deny access based on role

8. Response
   └─ If authorized: 200 OK + resource data
   └─ If unauthorized: 401 Unauthorized
   └─ If expired: 401 Unauthorized + "Token expired"
```

---

## 🔄 Error Handling Flows

```
SCENARIO 1: Email Already Registered
═══════════════════════════════════════
POST /auth/register {email: "admin@amazonlite.com", ...}
    │
    ├─► userRepository.findByEmail("admin@amazonlite.com")
    │   └─► Returns existing User object
    │
    └─► ❌ Email already registered
        Status: 409 Conflict
        Response: {
          "message": "Email already registered: admin@amazonlite.com"
        }

---

SCENARIO 2: Invalid Password on Login
═════════════════════════════════════════
POST /auth/login {email: "admin@amazonlite.com", password: "wrong"}
    │
    ├─► userRepository.findByEmail("admin@amazonlite.com")
    │   └─► Returns existing User
    │
    ├─► BCrypt.matches("wrong", "$2a$10$...")
    │   └─► Returns FALSE
    │
    └─► ❌ Invalid password
        Status: 401 Unauthorized
        Response: {
          "message": "Invalid credentials: Invalid password..."
        }

---

SCENARIO 3: User Not Found
════════════════════════════
POST /auth/login {email: "nonexistent@example.com", ...}
    │
    ├─► userRepository.findByEmail("nonexistent@example.com")
    │   └─► Optional.empty() - no user found
    │
    └─► ❌ User not found
        Status: 401 Unauthorized
        Response: {
          "message": "Invalid credentials: User not found..."
        }

---

SCENARIO 4: Expired Token
═════════════════════════════
GET /protected-resource
    Authorization: Bearer <old_token_from_2_days_ago>
    │
    ├─► JwtUtil.validateToken(token)
    │   ├─► Parse token
    │   ├─► Check: NOW() < exp?
    │   └─► ❌ NOW() > exp (token expired 24+ hours ago)
    │
    └─► ❌ Token expired
        Status: 401 Unauthorized
        Response: {
          "message": "Token expired"
        }
        Action: Client should redirect to login
```

---

## 🎨 Component Interaction Diagram

```
┌──────────────────┐
│  AuthController  │
│   (HTTP Layer)   │
│                  │
│ • /auth/register │
│ • /auth/login    │
│ • /auth/health   │
└────────┬─────────┘
         │
         │ calls
         │
         ▼
┌──────────────────┐
│   AuthService    │
│  (Business Logic)│
│                  │
│ • register()     │
│ • login()        │
│ • verifyRole()   │
└────────┬─────────┘
         │
         ├──────────┬──────────┐
         │          │          │
    uses │      uses│      uses│
         │          │          │
         ▼          ▼          ▼
    ┌──────┐   ┌──────┐   ┌──────────┐
    │BCrypt│   │JwtUtil│  │UserRepo  │
    │      │   │       │  │(JPA)     │
    │ Hash │   │Token  │  │          │
    │Verify│   │Gen/Val│  │findByEmail
    └──────┘   └──────┘   │save()    │
                          └────┬─────┘
                               │
                               │ queries/inserts
                               │
                               ▼
                        ┌──────────────────┐
                        │  PostgreSQL DB   │
                        │  (users table)   │
                        │                  │
                        │ • id             │
                        │ • email          │
                        │ • password_hash  │
                        │ • role           │
                        │ • created_at     │
                        └──────────────────┘
```

---

**Diagram Version**: 1.0
**Created**: May 6, 2026
**Status**: Documentation Complete

