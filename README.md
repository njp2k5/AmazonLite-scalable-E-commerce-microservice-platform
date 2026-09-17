# FAANG-Projects — AmazonLite + Chapter One

> A production-grade, full-stack e-commerce platform built with a **Spring Boot microservices backend** (AmazonLite) and a **Next.js 16 frontend** (Chapter One). Designed as a FAANG-level showcase project demonstrating distributed systems design, event-driven architecture, JWT security, and modern React/Next.js UI patterns.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [High-Level Architecture](#2-high-level-architecture)
3. [Repository Structure](#3-repository-structure)
4. [Backend — AmazonLite Microservices](#4-backend--amazonlite-microservices)
   - 4.1 [Technology Stack](#41-technology-stack)
   - 4.2 [Service Map & Port Assignments](#42-service-map--port-assignments)
   - 4.3 [Infrastructure & Shared Middleware](#43-infrastructure--shared-middleware)
   - 4.4 [Registry Service (Eureka)](#44-registry-service-eureka)
   - 4.5 [Gateway Service](#45-gateway-service)
   - 4.6 [Auth Service](#46-auth-service)
   - 4.7 [Product Service](#47-product-service)
   - 4.8 [Order Service](#48-order-service)
   - 4.9 [Notification Service](#49-notification-service)
   - 4.10 [Inventory Service](#410-inventory-service)
   - 4.11 [Cart Service](#411-cart-service)
5. [Database Design](#5-database-design)
6. [Event-Driven Messaging (Kafka)](#6-event-driven-messaging-kafka)
7. [Security — JWT Authentication & RBAC](#7-security--jwt-authentication--rbac)
8. [Cross-Cutting Concerns](#8-cross-cutting-concerns)
9. [Frontend — Chapter One (Next.js)](#9-frontend--chapter-one-nextjs)
   - 9.1 [Technology Stack](#91-technology-stack)
   - 9.2 [Application Pages & Routes](#92-application-pages--routes)
   - 9.3 [Component Architecture](#93-component-architecture)
   - 9.4 [State Management (Zustand)](#94-state-management-zustand)
   - 9.5 [Service Layer](#95-service-layer)
   - 9.6 [Type System](#96-type-system)
   - 9.7 [Design System & Aesthetics](#97-design-system--aesthetics)
10. [API Reference](#10-api-reference)
11. [Docker & Deployment](#11-docker--deployment)
12. [Environment Variables](#12-environment-variables)
13. [Seeding the Database](#13-seeding-the-database)
14. [From-Scratch Recreation Guide](#14-from-scratch-recreation-guide)
15. [Testing](#15-testing)

---

## 1. Project Overview

**AmazonLite** is a microservices backend inspired by Amazon's architecture. It separates business domains into independent Spring Boot services that communicate via REST (synchronous) and Apache Kafka (asynchronous). Service discovery is handled by Netflix Eureka, and all external traffic is routed through a Spring Cloud Gateway.

**Chapter One** is a premium bookstore frontend ("Where Every Story Begins"). It consumes the AmazonLite API gateway and provides a beautifully designed shopping experience with real-time search, a slide-out cart drawer, multi-step checkout, and full authentication.

Together they form a deployable, containerized full-stack application orchestrated through a single `docker-compose.yml`.

---

## 2. High-Level Architecture

```
+---------------------------------------------------------------------+
|                         CLIENT LAYER                                |
|          Next.js 16 Frontend — Chapter One (Port 3000)              |
|          (Browser -> HTTP -> API Gateway)                           |
+------------------------------+--------------------------------------+
                               | HTTP
                               v
+---------------------------------------------------------------------+
|                   API GATEWAY LAYER (Port 8080)                     |
|  Spring Cloud Gateway                                               |
|  +-----------------------------------------------------+           |
|  |  Global Filters                                     |           |
|  |  +- CorrelationIdGlobalFilter  (X-Correlation-Id)  |           |
|  |  +- JwtValidationFilter        (protected routes)  |           |
|  |  +- OrderAuthFilter            (/api/orders/**)    |           |
|  |  +- AuthLoggingFilter          (request logging)   |           |
|  +-----------------------------------------------------+           |
|  Route Table:                                                       |
|  /api/auth/**         -> auth-service     (public)                 |
|  /api/products/**     -> product-service  (public)                 |
|  /api/cart/**         -> cart-service     (JWT required)           |
|  /api/buy/**          -> product-service  (JWT required)           |
|  /api/orders/**       -> order-service    (JWT + OrderAuth)        |
+------------------------------+--------------------------------------+
                               | lb:// (Eureka load-balanced)
                               v
+---------------------------------------------------------------------+
|                    SERVICE DISCOVERY (Port 8761)                    |
|            Netflix Eureka Server — registry-service                 |
+------------------------------+--------------------------------------+
                               |
         +---------------------+-----------------------+
         v                     v                       v
+-----------------+   +-----------------+   +-----------------+
|  auth-service   |   | product-service |   |  order-service  |
|   Port 8082     |   |   Port 8081     |   |   Port 8083     |
|  PostgreSQL     |   |  PostgreSQL     |   |  PostgreSQL     |
|  JWT issuance   |   |  Books CRUD     |   |  Order mgmt     |
|  BCrypt hash    |   |  Search/filter  |   |  Stock deduct   |
|  RBAC           |   |  Pagination     |   |  Kafka publish  |
+-----------------+   +-----------------+   +--------+--------+
                                                     | Kafka
         +---------------------+-------------------  |
         v                     v                     v
+-----------------+   +-----------------+   +-----------------+
| notification-   |   |inventory-service|   |  cart-service   |
|   service       |   |   Port 8085     |   |   Port 8086     |
|   Port 8084     |   |  PostgreSQL     |   |  Redis + PG     |
| Kafka consumer  |   |  Stock track    |   |  Cart state     |
| Email sim.      |   |                 |   |                 |
+-----------------+   +-----------------+   +-----------------+
         |
         v
+---------------------------------------------------------------------+
|                    DATA / MESSAGE LAYER                             |
|  PostgreSQL 15  (shared DB: amazonlite)    Port 5432               |
|  Redis Alpine   (cart session cache)       Port 6379               |
|  Apache Kafka   (event bus)                Port 9092               |
+---------------------------------------------------------------------+
```

---

## 3. Repository Structure

```
FAANG-projects/
+-- AmazonLite/                       # Java Spring Boot microservices backend
|   +-- docker-compose.yml            # Orchestrates all 10 containers
|   +-- services/
|   |   +-- .env                      # Shared environment variables for all services
|   |   +-- registry-service/         # Eureka discovery server
|   |   +-- gateway-service/          # API gateway (Spring Cloud Gateway)
|   |   +-- auth-service/             # JWT auth, user registration/login
|   |   +-- product-service/          # Book catalog (CRUD, search, pagination)
|   |   +-- order-service/            # Order lifecycle + Kafka events
|   |   +-- notification-service/     # Kafka consumer, simulates email
|   |   +-- inventory-service/        # Stock tracking
|   |   +-- cart-service/             # Redis-backed shopping cart
|   +-- shared/
|   |   +-- common-lib/               # Shared Java library (future)
|   +-- infrastructure/
|   |   +-- docker/                   # Additional Docker configs
|   +-- seed.py                       # Python script to seed DB from local
|   +-- seed_docker.py                # Python script to seed DB in Docker
|   +-- seed_docker.sql               # SQL seed file
|   +-- generate_books.py             # Book data generator
|   +-- fix_seed.py                   # Fix/patch seed data
|   +-- fix_broken_images.py          # Image URL repair utility
|   +-- update_images.sql             # SQL for updating image URLs
|   +-- AUTH_ARCHITECTURE_DIAGRAMS.md # Detailed auth flow diagrams
|   +-- AUTH_IMPLEMENTATION_SUMMARY.md
|   +-- AUTH_QUICK_TESTING_GUIDE.md
|   +-- AUTH_SYSTEM_DOCUMENTATION.md
|
+-- ecommerce-frontend/               # Next.js 16 TypeScript frontend
|   +-- src/
|   |   +-- app/                      # Next.js App Router pages
|   |   |   +-- layout.tsx            # Root layout (fonts, providers, toasts)
|   |   |   +-- page.tsx              # Home page (landing)
|   |   |   +-- products/             # Book catalog listing + detail
|   |   |   |   +-- page.tsx          # /products — paginated book browser
|   |   |   |   +-- [id]/page.tsx     # /products/[id] — book detail page
|   |   |   +-- cart/page.tsx         # /cart — full cart page
|   |   |   +-- checkout/page.tsx     # /checkout — multi-step checkout
|   |   |   +-- orders/               # Order history + success page
|   |   |   +-- auth/
|   |   |   |   +-- login/page.tsx    # /auth/login
|   |   |   |   +-- register/page.tsx # /auth/register
|   |   |   +-- dashboard/            # Authenticated user dashboard
|   |   +-- components/
|   |   |   +-- layout/               # Navbar, Footer, HeroSection, etc.
|   |   |   +-- product/              # BookCard, BookGrid
|   |   |   +-- cart/                 # CartDrawer
|   |   |   +-- auth/                 # Auth form components
|   |   |   +-- checkout/             # Checkout step components
|   |   |   +-- dashboard/            # Dashboard widgets
|   |   |   +-- animations/           # Framer Motion wrappers
|   |   |   +-- effects/              # Visual effect components
|   |   |   +-- providers/            # QueryProvider, AuthGuard
|   |   |   +-- ui/                   # Radix UI / shadcn primitives
|   |   +-- store/
|   |   |   +-- auth.store.ts         # Zustand auth state (persisted)
|   |   |   +-- cart.store.ts         # Zustand cart state (persisted)
|   |   +-- services/
|   |   |   +-- auth.service.ts       # POST /api/auth/login|register
|   |   |   +-- product.service.ts    # GET /api/products/**
|   |   |   +-- order.service.ts      # POST /api/orders
|   |   |   +-- cart.service.ts       # Cart API calls
|   |   |   +-- inventory.service.ts  # Stock checks
|   |   +-- hooks/
|   |   |   +-- use-auth.ts           # Auth hook (login/register/logout)
|   |   |   +-- use-cart.ts           # Cart hook
|   |   |   +-- use-products.ts       # Product fetching hook
|   |   |   +-- use-utils.ts          # Utility hooks (debounce, etc.)
|   |   +-- lib/
|   |   |   +-- api-client.ts         # Axios/fetch wrapper with auth headers
|   |   |   +-- utils.ts              # cn(), formatPrice(), etc.
|   |   +-- types/index.ts            # All TypeScript interfaces
|   |   +-- schemas/auth.schema.ts    # Zod validation schemas
|   |   +-- constants/index.ts        # Routes, endpoints, categories
|   |   +-- vendor/                   # Vendored 3rd-party code
|   +-- tests/                        # Playwright E2E tests
|   +-- Dockerfile                    # Frontend container build
|   +-- next.config.ts                # Next.js configuration
|   +-- tailwind.config.ts            # Tailwind CSS v4 config
|   +-- package.json                  # Dependencies
|
+-- realtime-urban-intelligence-platform/
    +-- routing-service/              # (Separate project)
```

---

## 4. Backend — AmazonLite Microservices

### 4.1 Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 17 |
| Framework | Spring Boot | 3.1.5 – 3.2.0 |
| Service Discovery | Spring Cloud Netflix Eureka | 2022.0.4 |
| API Gateway | Spring Cloud Gateway | 2022.0.4 |
| ORM | Spring Data JPA + Hibernate | included with Boot |
| Database | PostgreSQL | 15-alpine |
| Cache | Redis | alpine |
| Message Broker | Apache Kafka | — |
| Security | JJWT (JSON Web Tokens) | 0.12.3 |
| Password Hashing | BCrypt (Spring Security) | — |
| Lombok | Lombok | latest compatible |
| API Docs | SpringDoc OpenAPI | 2.3.0 |
| Build Tool | Maven | — |
| Containerization | Docker + Docker Compose | — |

---

### 4.2 Service Map & Port Assignments

| Service | Container Name | Host Port | Role |
|---|---|---|---|
| registry-service | registry-service | 8761 | Eureka discovery |
| gateway-service | gateway-service | **8080** | API gateway, single entry point |
| auth-service | auth-service | 8082 | JWT auth, users |
| product-service | product-service | 8081 | Book catalog |
| order-service | order-service | 8083 | Orders, Kafka publisher |
| notification-service | notification-service | 8084 | Kafka consumer |
| inventory-service | inventory-service | 8085 | Stock tracking |
| cart-service | cart-service | 8086 | Shopping cart (Redis) |
| postgres | postgres | 5432 | Shared relational DB |
| redis | redis | 6379 | Cart session cache |
| ecommerce-frontend | ecommerce-frontend | 3000 | Next.js app |

All services share a single Docker bridge network `amazonlite-network`. All Java services register with Eureka, enabling load-balanced routing via `lb://service-name` URIs in the gateway.

---

### 4.3 Infrastructure & Shared Middleware

#### Shared Environment (`services/.env`)
All services pick up a common `.env` file injected via Docker Compose `env_file`. This includes:
- `JWT_SECRET` — Base64-encoded HMAC-SHA256 key (min 256 bits)
- `JWT_EXPIRATION` — Token TTL in milliseconds (default: `86400000` = 24 hours)
- `KAFKA_BOOTSTRAP_SERVERS` — Kafka bootstrap address

#### Docker Network
All containers join `amazonlite-network` (bridge driver). Internal service-to-service communication uses Docker DNS names (`http://auth-service:8082`). Eureka resolves these names to load-balanced instances.

#### Database
A single PostgreSQL 15 instance hosts all schemas. Tables are auto-created/updated by Hibernate (`ddl-auto: update`). Database name is `amazonlite`, user `postgres`, password `password`.

---

### 4.4 Registry Service (Eureka)

**Location:** `services/registry-service/`
**Port:** 8761
**Main Class:** `RegistryApplication.java`

The Eureka Server acts as the service directory. All other Spring Boot services are Eureka **clients** that register themselves on startup and periodically send heartbeats. The gateway uses Eureka to dynamically resolve `lb://service-name` URIs to actual host:port pairs.

**Key configuration:**
```yaml
eureka:
  client:
    register-with-eureka: false  # Server doesn't register itself
    fetch-registry: false
  server:
    enable-self-preservation: false
```

**Eureka Dashboard:** `http://localhost:8761` — shows all registered services, their instance IDs, and health status.

---

### 4.5 Gateway Service

**Location:** `services/gateway-service/`
**Port:** 8080
**Main Class:** `GatewayApplication.java`

The Spring Cloud Gateway is the **single entry point** for all external traffic. It performs routing, JWT validation, correlation ID injection, and request/response logging.

#### Route Configuration

Routes are defined programmatically in the `customRouteLocator` `@Bean`:

```
/api/auth/**     -> lb://auth-service     (public — no JWT filter)
/api/products/** -> lb://product-service  (public — no JWT filter)
/api/cart/**     -> lb://cart-service     (JWT filter applied)
/api/buy/**      -> lb://product-service  (JWT filter applied)
/api/orders/**   -> lb://order-service    (JWT filter + OrderAuthFilter)
```

All routes strip the `/api` prefix before forwarding (via `.stripPrefix(1)`).

OpenAPI proxy routes:
- `/product-service/v3/api-docs` -> product-service `/v3/api-docs`
- `/order-service/v3/api-docs` -> order-service `/v3/api-docs`
- `/notification-service/v3/api-docs` -> notification-service `/v3/api-docs`

#### Filters in Detail

**`CorrelationIdGlobalFilter`** (Highest priority global filter)
- Runs on every request before any other filter
- Checks for `X-Correlation-Id` header; if absent, generates a new `UUID`
- Adds correlation ID to the forwarded request and to the response headers
- Enables distributed tracing across all services

**`JwtValidationFilter`** (Applied per-route on protected routes)
- Extracts `Authorization: Bearer <token>` header
- Returns `401 Unauthorized` if header is missing or malformed
- Calls `JwtUtil.validateToken(token)` to parse and verify the JWT
- On success, mutates the request with three new headers forwarded downstream:
  - `X-User-Email` — the JWT subject (user's email)
  - `X-User-Role` — the `role` claim from the JWT
  - `X-User-Id` — the `userId` claim from the JWT
- On failure, returns JSON error: `{"error": "...", "status": 401}`

**`OrderAuthFilter`** (Applied only on `/api/orders/**`)
- Additional business logic filter for order routes, working in conjunction with `JwtValidationFilter`

**`AuthLoggingFilter`**
- Logs authentication-related information for debugging and audit

#### Gateway JwtUtil (`gateway/util/JwtUtil.java`)
A copy of the auth-service's JWT utility, kept in the gateway to avoid cross-service JAR dependencies. Uses the same shared secret to verify tokens issued by auth-service.

---

### 4.6 Auth Service

**Location:** `services/auth-service/`
**Port:** 8082
**Main Class:** `AuthApplication.java`
**Dependencies:** Spring Web, Spring Data JPA, PostgreSQL, Spring Cloud Eureka Client, JJWT 0.12.3, BCrypt, Lombok, Spring Validation

#### Responsibility
Issues JWT tokens after verifying credentials, and provides user registration with BCrypt password hashing. Supports three roles: `ADMIN`, `SELLER`, `CUSTOMER`.

#### Model — `User.java`

```java
@Entity
@Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;  // BCrypt hash, never stored as plain text

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public enum UserRole { ADMIN, SELLER, CUSTOMER }
}
```

#### Repository — `UserRepository.java`
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

#### JWT Utility — `JwtUtil.java`
- Constructor: creates a `SecretKey` from the `jwt.secret` config value using `Keys.hmacShaKeyFor(secret.getBytes())`
- `generateToken(Long userId, String email, String role)` — builds a JWT with:
  - Subject: `email`
  - Custom claim `userId`: user's database primary key
  - Custom claim `role`: `CUSTOMER | SELLER | ADMIN`
  - Issued at: `now()`
  - Expiration: `now() + jwt.expiration` (24 hours by default)
  - Signed with `HS256`
- `validateToken(String token)` -> `boolean`
- `extractAllClaims(String token)` -> `Claims`
- `extractEmail(String token)` -> `String` (the JWT subject)
- `extractUserId(String token)` -> `Long`
- `extractRole(String token)` -> `String`

#### Service — `AuthService.java`
**`register(RegisterRequest)`:**
1. Check email uniqueness in DB -> throw `IllegalArgumentException` if duplicate
2. Hash password with `new BCryptPasswordEncoder().encode(password)`
3. Create and save `User` entity with the specified role (defaults to `CUSTOMER`)
4. Generate JWT via `JwtUtil.generateToken()` for immediate sign-in
5. Return `LoginResponse` with token + user info

**`login(LoginRequest)`:**
1. Find user by email -> throw if not found
2. Verify password: `passwordEncoder.matches(plain, hash)` -> throw if mismatch
3. Generate JWT and return `LoginResponse`

**`verifyRole(String token, String requiredRole)`** — utility for role-based checks in downstream services

#### Controller — `AuthController.java`

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/auth/register` | Public | Register new user. Body: `{email, password, role?}`. Returns `201` + JWT |
| POST | `/auth/login` | Public | Login. Body: `{email, password}`. Returns `200` + JWT |
| GET | `/auth/health` | Public | Returns `{status: "Auth service is healthy"}` |

**DTOs:**
- `RegisterRequest` — `{ email, password, role? }` (role defaults to `CUSTOMER`)
- `LoginRequest` — `{ email, password }`
- `LoginResponse` — `{ token, userId, email, role, message }`

**Error Handling:**
- `IllegalArgumentException` -> `409 Conflict` (email exists) or `401 Unauthorized` (bad credentials)
- All exceptions -> `500 Internal Server Error` with message

---

### 4.7 Product Service

**Location:** `services/product-service/`
**Port:** 8081
**Main Class:** `ProductApplication.java`

#### Responsibility
Manages the book catalog. Supports full-text search, pagination, bestseller/featured flags, and stock decrement operations called by the Order Service.

#### Model — `Book.java`

```java
@Entity
@Table(name = "Books", indexes = { @Index(name = "idx_products_name", columnList = "name") })
public class Book {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200, unique = true)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    private double price;
    private int stock;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "is_bestseller") private boolean isBestseller = false;
    @Column(name = "is_featured")   private boolean isFeatured = false;
    @Column(name = "image_url", length = 500) private String imageUrl;

    @ElementCollection(fetch = EAGER)
    @CollectionTable(name = "book_images", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "image_url", length = 500)
    private List<String> images = new ArrayList<>();

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(nullable = false, length = 200)
    private String author;

    private String publisher;
    private String format;    // "Hardcover", "Paperback", "eBook"
    private Integer pages;
    private String language;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
```

Two tables: `Books` (main entity) and `book_images` (one-to-many image URLs via `@ElementCollection`).

#### Controller — `BookController.java`

| Method | Path | Query Params | Description |
|---|---|---|---|
| GET | `/products` | `page=0&size=10` | Paginated product list |
| GET | `/products/page` | `page=0&size=10` | Alternate paginated endpoint |
| GET | `/products/search` | `name, author, category, minPrice, maxPrice, page, size` | Multi-field search |
| GET | `/products/{id}` | — | Single product by ID |
| PATCH | `/products/{id}/decrement-stock` | — | Body: `{quantity: N}`. Reduces stock atomically |
| GET | `/products/bestsellers` | — | All books where `isBestseller=true` |
| GET | `/products/featured` | — | Single book where `isFeatured=true` |

**`decrementStock`** error cases:
- `400 Bad Request` if `quantity <= 0`
- `409 Conflict` if insufficient stock (`IllegalArgumentException` from service)
- `404 Not Found` if book ID doesn't exist

---

### 4.8 Order Service

**Location:** `services/order-service/`
**Port:** 8083
**Main Class:** `OrderServiceApplication.java`
**Dependencies:** Spring Web, Spring Data JPA, PostgreSQL, Spring Kafka, Spring WebFlux (WebClient), Spring Security, SpringDoc OpenAPI

#### Responsibility
Creates and manages orders. Coordinates with Product Service (stock validation + decrement) via HTTP, and publishes `OrderCreatedEvent` to Kafka after successful DB commit.

#### Models

**`Order.java`**
```java
@Entity @Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = IDENTITY) private Long id;
    private Long userId;
    @Column(nullable = false) private BigDecimal totalPrice;
    @Enumerated(EnumType.STRING) private OrderStatus status;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    @OneToMany(mappedBy = "order", cascade = ALL, orphanRemoval = true)
    private List<OrderItem> items;
}
```

**`OrderItem.java`**
```java
@Entity @Table(name = "order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name = "order_id") private Order order;
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
}
```

**`OrderStatus.java`** (enum): `PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED`

#### Clients (WebClient)

**`ProductClient.java`** — `GET /products/{id}` on product-service
**`ProductStockClient.java`** — `PATCH /products/{id}/decrement-stock` on product-service

Both use Spring WebFlux `WebClient` targeting `lb://product-service` for Eureka load balancing.

#### Service — `OrderService.java`

**`createOrder(Long userId, CreateOrderRequest request)`** — `@Transactional`
1. Validate `quantity > 0`
2. Fetch product via `ProductClient.getProductById(productId)`
3. Validate product exists and stock >= requested quantity
4. Parse `unitPrice` as `BigDecimal` from product response
5. Call `ProductStockClient.decrementStock(productId, quantity)` to reduce inventory
6. Create and save `Order` entity with status `PENDING`
7. Create and save `OrderItem` entity linked to the order
8. Register `TransactionSynchronization.afterCommit()` callback that publishes `OrderCreatedEvent` to Kafka **only after DB commit succeeds** — this prevents phantom events on rollback

**`getOrderByIdWithAuth(Long orderId, Long userId, String userRole)`**
- ADMIN: can view any order
- CUSTOMER: can only view their own orders (matched by `userId`)
- Others: `403 Forbidden`

**`cancelOrder(Long orderId, Long userId, String userRole)`** — `@Transactional`
- Owner or ADMIN can cancel
- Cannot cancel if status is `SHIPPED` or `DELIVERED` -> `409 Conflict`
- Sets status to `CANCELLED` and saves

#### Controller — `OrderController.java`

All endpoints read `X-User-Id` and `X-User-Role` from request headers (injected by the Gateway's `JwtValidationFilter`).

| Method | Path | Required Header | Description |
|---|---|---|---|
| POST | `/orders` | `X-User-Id` | Create an order. Body: `{productId, quantity}` |
| GET | `/orders/me` | `X-User-Id` | Get current user's orders (paginated, sorted by `createdAt DESC`) |
| GET | `/orders/{id}` | `X-User-Id`, `X-User-Role` | Get order by ID with auth check |
| PATCH | `/orders/{id}/cancel` | `X-User-Id`, `X-User-Role` | Cancel order |

---

### 4.9 Notification Service

**Location:** `services/notification-service/`
**Port:** 8084
**Main Class:** `NotificationServiceApplication.java`
**Dependencies:** Spring Web, Spring Kafka, SLF4J, Spring Kafka Test, SpringDoc OpenAPI

#### Responsibility
Subscribes to Kafka topics and processes events asynchronously. Currently simulates email notifications by logging structured JSON events.

#### Kafka Consumers

**`OrderCreatedEventConsumer.java`**
- Topic: `order.created`
- Consumer Group: `notification-service-group`
- On each message:
  1. Extracts `correlationId` from the event
  2. Sets SLF4J MDC with the correlation ID for log correlation
  3. Logs a structured JSON entry with correlationId, eventType, topic, orderId, timestamp
  4. Logs: `"Sending order confirmation for order {id} to user {userId}"`
  5. Clears MDC in `finally` block

**`NotificationConsumer.java`** — Generic notification event consumer for `notification-events` topic

**`DeadLetterTopicConsumer.java`** — Handles failed messages sent to the Dead Letter Topic for error recovery and manual inspection

#### Shared Event: `OrderCreatedEvent.java`
Duplicated in both `order-service` and `notification-service` under `com.amazonlite.shared.events` to avoid cross-service JAR dependencies:
```java
public class OrderCreatedEvent {
    private String orderId;
    private String userId;
    private BigDecimal totalPrice;
    private Instant createdAt;
    private String correlationId;
}
```

---

### 4.10 Inventory Service

**Location:** `services/inventory-service/`
**Port:** 8085
**Main Class:** `InventoryServiceApplication.java`

#### Responsibility
Tracks stock levels independently of the product catalog. Provides stock reservation and availability checks.

#### Model — `InventoryItem.java`
```java
@Entity
public class InventoryItem {
    @Id @GeneratedValue(strategy = IDENTITY) private Long id;
    private Long productId;      // foreign reference to product-service
    private Integer quantity;    // available stock
    private Integer reserved;    // reserved but not yet fulfilled
}
```

The inventory service is designed to eventually replace the stock field in the Product Service for more sophisticated inventory management with reservations.

---

### 4.11 Cart Service

**Location:** `services/cart-service/`
**Port:** 8086
**Dependencies:** Spring Web, Spring Data JPA, Spring Data Redis, PostgreSQL
**Main Class:** `CartServiceApplication.java`

The Cart Service is designed to persist cart state in **Redis** for fast read/write performance during shopping sessions, with PostgreSQL as durable storage. The current frontend handles cart state locally via Zustand (client-side). The cart-service is available for integration when server-side cart persistence is needed.

**Docker config for Redis connection:**
```yaml
environment:
  SPRING_REDIS_HOST: redis
  SPRING_REDIS_PORT: 6379
```

---

## 5. Database Design

All services share a single PostgreSQL instance (`amazonlite` database). Hibernate manages schema via `ddl-auto: update`.

### Tables

#### `users` (auth-service)
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PK |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| password_hash | VARCHAR(255) | NOT NULL |
| role | VARCHAR(50) | NOT NULL — ADMIN/SELLER/CUSTOMER |
| created_at | TIMESTAMP | NOT NULL |

#### `Books` (product-service)
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PK |
| name | VARCHAR(200) | NOT NULL, UNIQUE |
| description | VARCHAR(1000) | NOT NULL |
| price | DOUBLE | NOT NULL |
| stock | INTEGER | NOT NULL |
| category | VARCHAR(100) | NOT NULL |
| is_bestseller | BOOLEAN | NOT NULL, DEFAULT false |
| is_featured | BOOLEAN | NOT NULL, DEFAULT false |
| image_url | VARCHAR(500) | nullable |
| isbn | VARCHAR(20) | NOT NULL, UNIQUE |
| author | VARCHAR(200) | NOT NULL |
| publisher | VARCHAR(200) | nullable |
| format | VARCHAR(50) | nullable |
| pages | INTEGER | nullable |
| language | VARCHAR(50) | nullable |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | nullable |

**Index:** `idx_products_name` on `name`

#### `book_images` (product-service — element collection)
| Column | Type | Constraints |
|---|---|---|
| book_id | BIGINT | FK -> Books.id |
| image_url | VARCHAR(500) | — |

#### `orders` (order-service)
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PK |
| user_id | BIGINT | — |
| total_price | DECIMAL | NOT NULL |
| status | VARCHAR | NOT NULL (PENDING/CONFIRMED/etc.) |
| created_at | TIMESTAMP | NOT NULL |

#### `order_items` (order-service)
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PK |
| order_id | BIGINT | FK -> orders.id |
| product_id | BIGINT | — |
| quantity | INTEGER | — |
| unit_price | DECIMAL | — |

---

## 6. Event-Driven Messaging (Kafka)

### Topic: `order.created`

**Publisher:** `OrderEventPublisher.java` in order-service
**Consumer:** `OrderCreatedEventConsumer.java` in notification-service

**Complete Flow:**
```
User places order
    |
    v
OrderService.createOrder() — @Transactional
    |
    +-> Validate quantity > 0
    +-> ProductClient: GET /products/{id}
    +-> Validate stock >= quantity
    +-> ProductStockClient: PATCH /products/{id}/decrement-stock
    +-> Create Order (status=PENDING) -> DB
    +-> Create OrderItem -> DB
    +-> Register afterCommit() callback
    |
    v (DB commits successfully)
    |
    v
afterCommit() fires:
    |
    +-> KafkaTemplate.send("order.created", OrderCreatedEvent)
                             |
                             v
    notification-service OrderCreatedEventConsumer
                             |
                             +-> Log structured JSON event
                             +-> Simulate sending confirmation email
```

**Why `afterCommit()`?** Publishing after the transaction commits guarantees that if the DB transaction rolls back (e.g., stock decrement fails), no Kafka event is published. This prevents consumers from processing events for non-existent orders — a critical correctness guarantee in distributed systems.

### `OrderCreatedEvent` JSON Schema
```json
{
  "orderId": "42",
  "userId": "7",
  "totalPrice": 29.99,
  "createdAt": "2026-09-16T13:30:00Z",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## 7. Security — JWT Authentication & RBAC

### Token Structure
JWT tokens are HS256-signed with a Base64-encoded secret key:
```json
{
  "sub": "user@example.com",
  "userId": 7,
  "role": "CUSTOMER",
  "iat": 1726489200,
  "exp": 1726575600
}
```

### Authentication Flow
```
1. Client: POST /api/auth/login { email, password }
   -> (gateway strips /api prefix)
2. auth-service: AuthController.login()
   -> AuthService.login()
3. Find user by email in users table
4. BCryptPasswordEncoder.matches(plain, hash)
   -> (if valid)
5. JwtUtil.generateToken(userId, email, role)
6. Return 200 OK { token, userId, email, role, message }
```

### Request Authorization Flow
```
1. Client: GET /api/orders/me
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
   |
2. Gateway: CorrelationIdGlobalFilter adds X-Correlation-Id
   |
3. Gateway: JwtValidationFilter
   +- Extract token from "Authorization: Bearer <token>"
   +- JwtUtil.validateToken(token) — parse + verify signature
   +- Extract claims: sub, userId, role
   +- Mutate request: add X-User-Email, X-User-Role, X-User-Id headers
   |
4. order-service: OrderController.getMyOrders()
   @RequestHeader("X-User-Id") Long userId
   -> OrderService.getOrdersByUserId(userId, pageable)
```

### Role-Based Access Control (RBAC)

| Role | Permissions |
|---|---|
| `CUSTOMER` | Browse products, manage own cart, create orders, view own orders, cancel own orders |
| `SELLER` | All CUSTOMER permissions + manage product listings |
| `ADMIN` | All permissions + view any order, cancel any order |

---

## 8. Cross-Cutting Concerns

### Distributed Tracing (Correlation ID)
Every request entering the gateway receives an `X-Correlation-Id` header (UUID). This ID:
1. Is added to all forwarded requests by `CorrelationIdGlobalFilter` (highest priority)
2. Is added to all response headers
3. Is stored in SLF4J MDC in downstream services via each service's `CorrelationIdFilter`
4. Is embedded in Kafka events via `OrderCreatedEvent.correlationId`
5. Is used by notification-service to log events with the same trace ID

This allows full end-to-end tracing of a single user request across all services.

### Structured Logging
The notification-service uses structured JSON logging:
```json
{
  "correlationId": "...",
  "eventType": "OrderCreated",
  "topic": "order.created",
  "orderId": "...",
  "timestamp": "..."
}
```

### Error Handling

| Layer | Mechanism |
|---|---|
| Gateway | `respondWithError()` returns JSON `{"error": "...", "status": HTTP_CODE}` |
| Services | `ResponseStatusException` maps to appropriate HTTP status codes |
| Auth Service | try/catch in controller converts exceptions to specific HTTP responses |
| Order Service | `@Transactional` rolls back on unchecked exceptions |

---

## 9. Frontend — Chapter One (Next.js)

### 9.1 Technology Stack

| Category | Library | Version |
|---|---|---|
| Framework | Next.js | ^16.2.9 |
| Language | TypeScript | ^6.0.3 |
| React | React + React DOM | ^19.2.7 |
| Styling | Tailwind CSS | v4.3.0 |
| State | Zustand | ^5.0.14 |
| Data Fetching | TanStack Query | ^5.101.0 |
| UI Primitives | Radix UI (30+ packages) | latest |
| Animations | Motion (Framer Motion) | ^12.40.0 |
| Forms | React Hook Form | ^7.78.0 |
| Validation | Zod | ^4.4.3 |
| Icons | Lucide React | ^1.17.0 |
| Toasts | react-hot-toast | ^2.6.0 |
| Fonts | Inter, Playfair Display, Geist Mono | Google Fonts |
| Charts | Recharts | ^3.8.1 |
| Image Zoom | react-medium-image-zoom | ^5.4.7 |
| Carousel | embla-carousel-react | ^8.6.0 |
| E2E Testing | Playwright | ^1.60.0 |
| Containerization | Docker (multi-stage) | — |

---

### 9.2 Application Pages & Routes

All pages use the **Next.js 16 App Router** with the `app/` directory convention.

#### Root Layout (`app/layout.tsx`)
- Loads three Google Fonts as CSS variables: `--font-inter`, `--font-playfair`, `--font-geist-mono`
- Wraps children with `QueryProvider` (TanStack Query client)
- Renders `react-hot-toast` `<Toaster>` with custom dark theme (charcoal background `#1F2937`, gold accent `#C6A969`)
- Full SEO metadata: title template, description, Open Graph tags, Twitter card, robots meta

#### Pages

| Route | File | Access | Description |
|---|---|---|---|
| `/` | `app/page.tsx` | Public | Full landing page with all homepage sections |
| `/products` | `app/products/page.tsx` | Public | Paginated book catalog with search & category filters |
| `/products/[id]` | `app/products/[id]/page.tsx` | Public | Single book detail with add-to-cart |
| `/cart` | `app/cart/page.tsx` | Public | Full cart page view |
| `/checkout` | `app/checkout/page.tsx` | Auth required | 3-step checkout (Shipping -> Payment -> Review) |
| `/orders` | `app/orders/page.tsx` | Auth required | Paginated order history |
| `/orders/success/[id]` | `app/orders/...` | Auth required | Order success confirmation screen |
| `/auth/login` | `app/auth/login/page.tsx` | Public | Sign in form with React Hook Form + Zod |
| `/auth/register` | `app/auth/register/page.tsx` | Public | Sign up form |
| `/dashboard` | `app/dashboard/page.tsx` | Auth required | User dashboard with order stats |

**Home page sections (in order):**
1. `Navbar` — Fixed top navigation
2. `HeroSection` — 3D book card + floating books + headline + CTA
3. `BestsellersSection` — Horizontal scroll of bestseller BookCards
4. `CategoriesSection` — Category grid linking to `/products?category=`
5. `AuthorSpotlightSection` — Featured authors
6. `StatsSection` — Animated platform statistics
7. `TestimonialsSection` — Customer reviews carousel
8. `NewsletterSection` — Email signup
9. `Footer` — Site map, social links

---

### 9.3 Component Architecture

#### `Navbar.tsx` (423 lines)

State: `scrolled`, `mobileOpen`, `searchOpen`, `searchQuery`, `searchResults`, `searchLoading`, `activeDropdown`, `profileOpen`

Features:
- Scroll-aware glassmorphism: transparent on top, `glass-ivory` (backdrop-blur) when `scrollY > 20`
- Dropdown navigation with hover-triggered sub-menus (Browse -> Fiction, Non-Fiction, etc.)
- **Live search**: 350ms debounced search via `productService.search()`, shows up to 6 results in a floating modal overlay
- Cart badge with item count from `useCartStore`
- Auth-aware user area: Sign In button for guests; avatar + name + dropdown (Dashboard, My Orders, Sign Out) for authenticated users
- Full mobile responsive menu
- Renders `CartDrawer` at the end (so it's always in DOM)

#### `HeroSection.tsx` (410 lines)

Features:
- Full-viewport section with warm cream gradient background
- Mouse-tracking cursor spotlight: `radial-gradient` follows the cursor position via `onMouseMove`
- Loads featured book from `productService.getFeatured()` on mount
- Left panel: headline with italic serif typography, dual CTA buttons, social proof (avatars + star ratings)
- Right panel: `FeaturedBookCard` with 3D perspective tilt effect based on mouse position (`rotateX`/`rotateY`)
- 4 floating decorative mini book spines (`MINI_BOOKS` array) with staggered `animate-float` CSS animations
- Wave SVG divider at the bottom

#### `FeaturedBookCard` (sub-component inside HeroSection)

- 3D tilt: `perspective: 1000px`, `rotateX(${tilt.x}deg) rotateY(${tilt.y}deg)` calculated from mouse position relative to card bounds
- Book cover: gradient fallback with palette derived from title hash, overlaid with actual cover image if available
- Dark gradient overlay on cover for text readability
- Floating gold sparkle badge in top-right corner with `animate-float`
- Info panel below cover: "Featured Pick" label, price, "Read More" button

#### `CartDrawer.tsx` (212 lines)

- Controlled by `useCartStore.isOpen`
- Blurred backdrop overlay
- Slide animation: `translate-x-full` -> `translate-x-0` with 400ms ease
- Width: full on mobile, `420px` on `sm+`
- Shows all cart items with: mini book cover, title, author, quantity controls, total price, remove button
- Footer summary: subtotal, shipping (free vs paid), tax (10%), grand total
- "Proceed to Checkout" -> `/checkout`
- "View full cart" -> `/cart`

#### `BookCard.tsx`
- Displays book cover (gradient fallback on error)
- Shows title, author, category badge, price
- "Add to Cart" button calls `useCartStore.addItem(book)` and opens the cart drawer
- Links to `/products/[id]` for detail view
- Hover: scale + shadow transition

#### `BookGrid.tsx`
- Responsive CSS grid with configurable columns prop
- Loading state: animated shimmer skeleton placeholders
- Empty state: centered message

#### `AuthGuard.tsx`
- Checks `useAuthStore.isAuthenticated`
- Redirects to `/auth/login` if not authenticated
- Used as a wrapper around `CheckoutPage` and dashboard pages

---

### 9.4 State Management (Zustand)

Both stores use Zustand with the `persist` middleware for `localStorage` persistence.

#### Auth Store (`store/auth.store.ts`)

```typescript
interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login(token: string): void;
  logout(): void;
  setLoading(loading: boolean): void;
  updateUser(updates: Partial<User>): void;
}
```

**`login(token)` flow:**
1. Store token in `localStorage[STORAGE_KEYS.AUTH_TOKEN]`
2. `decodeJwtPayload(token)` — base64 decode JWT payload without verification (client-side only)
3. Construct `User` from claims: `sub`->`id`, `email`, `name`/`email`->`name`, `role`
4. Set `isAuthenticated: true`

**Persisted keys:** `user`, `token`, `isAuthenticated` (stored as `auth-storage` in localStorage)

#### Cart Store (`store/cart.store.ts`)

```typescript
interface CartState {
  cart: Cart | null;
  isLoading: boolean;
  isOpen: boolean;
  setCart(cart): void;
  openCart(): void; closeCart(): void; toggleCart(): void;
  addItem(book: Book, quantity?: number): void;
  removeItem(itemId: string): void;
  updateQuantity(itemId: string, quantity: number): void;
  clearCart(): void;
  getItemCount(): number;
  getSubtotal(): number;
}
```

**Cart Calculation (`recalculate(cart)`):**
- `subtotal` = sum of `item.quantity * item.unitPrice`
- `tax` = `subtotal * 0.10` (10%)
- `shipping` = `subtotal > 100 ? 0 : 9.99` (free over $100)
- `total` = `subtotal + tax + shipping - discount`
- `itemCount` = sum of all `item.quantity`

**`addItem` behavior:** If book already in cart, increments quantity; otherwise pushes a new `CartItem` with a randomly generated ID.

**Persisted keys:** `cart` (stored as `cart-storage` in localStorage)

---

### 9.5 Service Layer

All services use a shared `apiClient` configured with the `NEXT_PUBLIC_API_URL` base URL. Protected calls automatically attach `Authorization: Bearer <token>` from localStorage.

#### `product.service.ts`
```typescript
productService.getAll()                    // GET /api/products
productService.getById(id)                // GET /api/products/{id}
productService.search(query)              // GET /api/products/search?query=
productService.getPaginated(page, size)   // GET /api/products/page?page=&size=
productService.decrementStock(id)         // PATCH /api/products/{id}/decrement-stock
productService.getBestsellers()           // GET /api/products/bestsellers
productService.getFeatured()              // GET /api/products/featured
```

A `mapBook()` normalizer applies to every response to bridge backend field naming:
- `book.name` -> `book.title` (backend uses `name`, frontend types use `title`)
- `book.imageUrl` -> `book.coverImage`

#### `auth.service.ts`
```typescript
authService.login({ email, password })     // POST /api/auth/login
authService.register({ name, email, password }) // POST /api/auth/register
authService.health()                       // GET /api/auth/health
decodeJwtPayload(token)                    // Utility: base64 decode JWT payload
```

#### `order.service.ts`
```typescript
orderService.create(payload)              // POST /api/orders (auth required)
orderService.getMyOrders()               // GET /api/orders/me
orderService.getById(id)                // GET /api/orders/{id}
orderService.cancel(id)                 // PATCH /api/orders/{id}/cancel
```

---

### 9.6 Type System

All TypeScript interfaces are in `src/types/index.ts`:

```typescript
interface User { id, email, name, avatar?, role: "customer"|"admin", createdAt?, updatedAt? }
interface Book { id, title, author, description, price, category, stock, rating?, 
                  coverImage?, imageUrl?, images?, isbn?, publisher?, pageCount?, language? }
interface CartItem { id, book: Book, quantity, unitPrice, totalPrice }
interface Cart { id, items: CartItem[], subtotal, tax, shipping, discount, total, itemCount, updatedAt }
type OrderStatus = "PENDING"|"CONFIRMED"|"PROCESSING"|"SHIPPED"|"DELIVERED"|"CANCELLED"|"REFUNDED"
interface Order { id, items: OrderItem[], status: OrderStatus, total, createdAt, shippingAddress? }
interface PaginatedResponse<T> { content?, data?, totalElements?, totalPages?, size?, number? }
interface BookFilters { query?, category?, minPrice?, maxPrice?, page?, size? }
interface LoginApiResponse { token: string }
```

**Zod Schemas (`schemas/auth.schema.ts`):**
```typescript
loginSchema = z.object({ email: z.string().email(), password: z.string().min(6) })
registerSchema = z.object({ name: z.string().min(2), email: z.string().email(), password: z.string().min(6) })
```

---

### 9.7 Design System & Aesthetics

#### Color Palette
| Token | Hex | Usage |
|---|---|---|
| Charcoal | `#1F2937` | Primary text, dark buttons, dark backgrounds |
| Gold | `#C6A969` | Accent, badges, prices, borders, hover states |
| Cream | `#F8F5F0` | Page background, card backgrounds |
| Warm Gray | `#EDE8DF` | Section dividers, alternate backgrounds |
| Bronze | `#8B7355` | Secondary text, muted elements |

#### Typography
- **Headings:** `Playfair Display` (serif) — editorial, literary feel
- **Body:** `Inter` (sans-serif) — modern, highly legible
- **Code/Mono:** `Geist Mono`

#### Custom CSS Classes (`globals.css`)
- `glass-ivory` — backdrop-blur + semi-transparent white + border
- `shadow-warm` / `shadow-warm-lg` — warm-toned box shadows
- `shadow-gold` — gold-tinted glow
- `shadow-book-hover` — multi-layer book depth shadow
- `text-gold-gradient` — gold-to-warm-gold CSS gradient text
- `divider-gold` — thin gold separator
- `animate-float` — CSS keyframe floating animation
- `animate-fade-up` — fade in + slide up on load

#### Micro-Animations
- **Navbar**: scroll-triggered `glass-ivory` glassmorphism transition
- **Hero**: mouse-tracking radial gradient spotlight cursor effect
- **Hero Book Card**: 3D `rotateX/rotateY` tilt on mouse hover
- **Cart Drawer**: `translate-x-full` -> `translate-x-0` slide (400ms ease)
- **Dropdowns**: `ChevronDown` rotates 180deg on open
- **Floating Books**: `animate-float` keyframe with staggered delays
- **Book Cards**: scale + shadow on hover via CSS transition

---

## 10. API Reference

All requests go through the Gateway at `http://localhost:8080`.

### Authentication

| Method | URL | Body | Response |
|---|---|---|---|
| POST | `/api/auth/register` | `{email, password, role?}` | `{token, userId, email, role, message}` |
| POST | `/api/auth/login` | `{email, password}` | `{token, userId, email, role, message}` |
| GET | `/api/auth/health` | — | `{status: "Auth service is healthy"}` |

### Products (Public)

| Method | URL | Query Params | Response |
|---|---|---|---|
| GET | `/api/products` | `page=0&size=10` | `{content: Book[], totalPages, ...}` |
| GET | `/api/products/page` | `page=0&size=10` | Paginated books |
| GET | `/api/products/search` | `name, author, category, minPrice, maxPrice, page, size` | Search results |
| GET | `/api/products/{id}` | — | Single `Book` |
| GET | `/api/products/bestsellers` | — | `Book[]` where `isBestseller=true` |
| GET | `/api/products/featured` | — | Single featured `Book` |
| PATCH | `/api/products/{id}/decrement-stock` | — | Body: `{quantity: N}` -> `{stock: newCount}` |

### Orders (JWT Required — `Authorization: Bearer <token>`)

| Method | URL | Body | Response |
|---|---|---|---|
| POST | `/api/orders` | `{productId, quantity}` | `{orderId, status, totalPrice}` |
| GET | `/api/orders/me` | — | Paginated `Order[]` |
| GET | `/api/orders/{id}` | — | Single `Order` |
| PATCH | `/api/orders/{id}/cancel` | — | Updated `Order` |

### OpenAPI Documentation
- `http://localhost:8080/product-service/v3/api-docs` — Product service
- `http://localhost:8080/order-service/v3/api-docs` — Order service
- `http://localhost:8080/notification-service/v3/api-docs` — Notification service

---

## 11. Docker & Deployment

### Prerequisites
- Docker Desktop (Docker Compose v3.8+)
- At least 8GB RAM allocated to Docker
- Ports available: 3000, 5432, 6379, 8080–8086, 8761

### Start All Services

```bash
cd AmazonLite
docker-compose up --build
```

This will:
1. Build Docker images for all 8 Spring Boot services + Next.js frontend
2. Start PostgreSQL + Redis
3. Start `registry-service` (Eureka) first
4. Start all microservices (depends_on: registry-service + postgres)
5. Start `gateway-service` (depends_on: all microservices)
6. Start `ecommerce-frontend` (port 3000)

### Service Startup Order

Defined by `depends_on` in docker-compose.yml:
```
postgres, redis, registry-service
    -> auth-service, product-service, order-service, inventory-service, notification-service, cart-service
        -> gateway-service
            -> ecommerce-frontend
```

Services need ~30-60 seconds to start and register with Eureka before they are routable.

### Stop & Clean Up

```bash
docker-compose down             # Stop containers, preserve volumes
docker-compose down -v          # Stop + remove volumes (wipes DB)
docker-compose down --rmi all   # Stop + remove all images
```

### Health Check URLs

| Service | URL |
|---|---|
| Eureka Dashboard | http://localhost:8761 |
| Auth Health | http://localhost:8080/api/auth/health |
| Products API | http://localhost:8080/api/products?page=0&size=5 |
| Frontend | http://localhost:3000 |
| Product OpenAPI | http://localhost:8080/product-service/v3/api-docs |

---

## 12. Environment Variables

### Backend (`AmazonLite/services/.env`)

```env
# JWT
JWT_SECRET=dGhpcy1pcy1hLXZlcnktc2VjdXJlLWtleS1mb3Itand0LW11c3QtYmUtYXQtbGVhc3QtMjU2LWJpdHM=
JWT_EXPIRATION=86400000

# Database (overridden per-service in docker-compose.yml)
DB_URL=jdbc:postgresql://localhost:5432/amazonlite
DB_USERNAME=postgres
DB_PASSWORD=password

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
```

> **Security Note:** Always override `JWT_SECRET` with a strong, randomly generated 256-bit key in any environment beyond local development.

### Frontend (`ecommerce-frontend/.env.local`)

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

The `NEXT_PUBLIC_` prefix makes this variable available in the browser bundle (Next.js convention).

### Docker Compose per-service overrides

Each service receives service-specific variables that point to Docker container hostnames:
```yaml
environment:
  SPRING_APPLICATION_NAME: auth-service
  EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: "http://registry-service:8761/eureka/"
  DB_URL: "jdbc:postgresql://postgres:5432/amazonlite"
  # ...
```

The `postgres` hostname resolves to the PostgreSQL container within the `amazonlite-network`.

---

## 13. Seeding the Database

Python scripts populate the database with realistic book data.

### Prerequisites
```bash
pip install psycopg2-binary requests
```

### Seed from Local PostgreSQL (port 5432 on localhost)
```bash
cd AmazonLite
python seed.py
```

### Seed from Dockerized PostgreSQL
```bash
cd AmazonLite
docker-compose up -d postgres
python seed_docker.py
```

### SQL Direct Import
```bash
# With Docker Compose running:
docker exec -i postgres psql -U postgres -d amazonlite < seed_docker.sql
```

### Utility Scripts Reference

| Script | Purpose |
|---|---|
| `generate_books.py` | Generate sample book JSON data with realistic metadata |
| `fix_seed.py` | Fix/patch existing seeded data (e.g., missing fields) |
| `fix_broken_images.py` | Repair broken image URLs by substituting fallbacks |
| `fix_featured.py` | Set the `is_featured=true` flag on a specific book |
| `update_images.sql` | Bulk SQL update for image URLs |
| `delete_books.py` | Remove all books from the Books table |
| `list_all_books.py` | Print all books with their IDs, titles, and stock |
| `check_db.py` | Verify DB connection and print table row counts |
| `check_channel.py` | Test Kafka connectivity |
| `alter_db.py` | Apply schema alterations (e.g., adding new columns) |
| `verify_db.py` | Verify data integrity across tables |

---

## 14. From-Scratch Recreation Guide

This is a complete step-by-step guide to recreate the entire project.

### Prerequisites
- Java JDK 17 (with `JAVA_HOME` set)
- Maven 3.9+
- Node.js 20+ with npm
- Docker Desktop
- Python 3.11+ with `psycopg2-binary` and `requests`

---

### Step 1: Create the Project Directory Structure

```bash
mkdir FAANG-projects
cd FAANG-projects
mkdir AmazonLite
mkdir AmazonLite/services
mkdir AmazonLite/shared
mkdir AmazonLite/infrastructure
```

---

### Step 2: Create the Registry Service (Eureka Server)

```bash
cd AmazonLite/services
# Use Spring Initializr or create manually
mkdir registry-service && cd registry-service
```

**pom.xml (key additions):**
```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

**RegistryApplication.java:**
```java
@SpringBootApplication
@EnableEurekaServer
public class RegistryApplication {
    public static void main(String[] args) {
        SpringApplication.run(RegistryApplication.class, args);
    }
}
```

**application.yml:**
```yaml
server:
  port: 8761
spring:
  application:
    name: registry-service
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  server:
    enable-self-preservation: false
```

---

### Step 3: Create the Auth Service

**Package structure:** `com.amazonlite.auth.{controller,dto,model,repository,security,service}`

1. **`User.java`** — JPA entity with fields: `id`, `email`, `passwordHash`, `role (enum)`, `createdAt`
2. **`UserRepository.java`** — extends `JpaRepository<User, Long>`, method: `findByEmail(String email)`
3. **`JwtUtil.java`** — uses JJWT 0.12.3: `generateToken()`, `validateToken()`, `extractAllClaims()`, `extractEmail()`, `extractUserId()`, `extractRole()`
4. **`AuthService.java`** — `register()` (check duplicate email -> BCrypt hash -> save -> generateToken), `login()` (find user -> BCrypt match -> generateToken)
5. **DTOs:** `LoginRequest`, `LoginResponse`, `RegisterRequest`
6. **`AuthController.java`** — `POST /auth/register`, `POST /auth/login`, `GET /auth/health`

**application.yml:**
```yaml
server:
  port: 8082
spring:
  application:
    name: auth-service
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE:http://localhost:8761/eureka/}
jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:86400000}
```

---

### Step 4: Create the Product Service

**Package structure:** `com.amazonlite.product.{controller,model,repository,service}`

1. **`Book.java`** — JPA entity mapped to `Books` table with all book fields (name, description, price, stock, category, isBestseller, isFeatured, imageUrl, images via @ElementCollection, isbn, author, publisher, format, pages, language, createdAt, updatedAt)
2. **`BookRepository.java`** — extends `JpaRepository<Book, Long>` with custom query methods for search/filter
3. **`BookService.java`** — implements `getProducts()`, `getPaginated()`, `searchBooks()`, `getById()`, `decrementStock()`, `getBestsellers()`, `getFeatured()`
4. **`BookController.java`** — all public endpoints

**application.yml:** port 8081, same DB and Eureka config as auth-service.

---

### Step 5: Create the Order Service

**Package structure:** `com.amazonlite.order.{client,config,controller,dto,event,model,repository,service}` and `com.amazonlite.shared.events`

1. **Models:** `Order`, `OrderItem`, `OrderStatus` (enum)
2. **DTOs:** `CreateOrderRequest` (`productId`, `quantity`), `CreateOrderResponse` (`orderId`, `status`, `totalPrice`)
3. **Clients:** `ProductClient` (GET product by ID), `ProductStockClient` (PATCH decrement-stock) — both using `WebClient` with Eureka load balancing
4. **`WebClientConfig.java`** — configure `WebClient.Builder` with `@LoadBalanced`
5. **`KafkaProducerConfig.java`** — configure `KafkaTemplate<String, Object>` with JSON serializer
6. **`OrderCreatedEvent.java`** in `com.amazonlite.shared.events` — plain POJO with `orderId`, `userId`, `totalPrice`, `createdAt`, `correlationId`
7. **`OrderEventPublisher.java`** — wraps `KafkaTemplate.send("order.created", event)`
8. **`OrderService.java`** — `createOrder()` with `@Transactional` + `afterCommit()` Kafka publish; `getOrderByIdWithAuth()`; `cancelOrder()`
9. **`OrderController.java`** — all endpoints reading `@RequestHeader("X-User-Id")`
10. **`CorrelationIdFilter.java`** — servlet filter that reads `X-Correlation-Id` and puts it in MDC

**application.yml:** port 8083 + Kafka bootstrap server config.

---

### Step 6: Create the Notification Service

**Package structure:** `com.amazonlite.notification.{config,consumer,controller,dto,service}` and `com.amazonlite.shared.events`

1. Copy `OrderCreatedEvent.java` to `com.amazonlite.shared.events` (must match package and fields exactly)
2. **`KafkaConfig.java`** — configure `ConcurrentKafkaListenerContainerFactory` with `JsonDeserializer` for `OrderCreatedEvent`, trusted packages: `com.amazonlite.shared.events`
3. **`OrderCreatedEventConsumer.java`** — `@KafkaListener(topics = "order.created", groupId = "notification-service-group")`, log structured event + simulate email
4. **`NotificationConsumer.java`** — generic consumer for `notification-events` topic
5. **`DeadLetterTopicConsumer.java`** — handle failed message processing
6. **`NotificationController.java`** — health check endpoint

---

### Step 7: Create the Gateway Service

**Package structure:** `com.amazonlite.gateway.{controller,filter,util}`

1. **`JwtUtil.java`** in `gateway/util/` — identical to auth-service's JwtUtil (copy it; validates tokens using same shared secret)
2. **`CorrelationIdGlobalFilter.java`** — implements `GlobalFilter, Ordered`; `getOrder()` returns `Ordered.HIGHEST_PRECEDENCE`; generates/propagates UUID correlation ID
3. **`JwtValidationFilter.java`** — extends `AbstractGatewayFilterFactory<Config>`; extracts bearer token; calls JwtUtil.validateToken(); on success adds `X-User-Email`, `X-User-Id`, `X-User-Role` headers; on failure returns `401`
4. **`OrderAuthFilter.java`** — extends `AbstractGatewayFilterFactory<Config>`; order-specific auth logic
5. **`AuthLoggingFilter.java`** — request/response logging
6. **`GatewayApplication.java`** — define all routes in `customRouteLocator` @Bean

**application.yml:** port 8080; `spring.cloud.gateway.discovery.locator.enabled: true`

**pom.xml additions:**
```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.12.3</version>
</dependency>
```

---

### Step 8: Add Dockerfiles

For each Spring Boot service (`auth`, `product`, `order`, `notification`, `inventory`, `cart`, `registry`, `gateway`):

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE <PORT>
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### Step 9: Create `docker-compose.yml`

```yaml
version: '3.8'
services:
  registry-service:
    build: ./services/registry-service
    ports: ["8761:8761"]
    networks: [amazonlite-network]

  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: amazonlite
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports: ["5432:5432"]
    networks: [amazonlite-network]

  redis:
    image: redis:alpine
    ports: ["6379:6379"]
    networks: [amazonlite-network]

  auth-service:
    build: ./services/auth-service
    ports: ["8082:8082"]
    env_file: [./services/.env]
    environment:
      SPRING_APPLICATION_NAME: auth-service
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: "http://registry-service:8761/eureka/"
      DB_URL: "jdbc:postgresql://postgres:5432/amazonlite"
      DB_USERNAME: postgres
      DB_PASSWORD: password
    depends_on: [registry-service, postgres]
    networks: [amazonlite-network]

  # ... repeat pattern for product-service (8081), order-service (8083),
  #     inventory-service (8085), notification-service (8084),
  #     cart-service (8086, add redis dependency)

  gateway-service:
    build: ./services/gateway-service
    ports: ["8080:8080"]
    env_file: [./services/.env]
    environment:
      SPRING_APPLICATION_NAME: gateway-service
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: "http://registry-service:8761/eureka/"
    depends_on: [registry-service, auth-service, product-service, order-service,
                 inventory-service, notification-service, cart-service]
    networks: [amazonlite-network]

  ecommerce-frontend:
    build: ../ecommerce-frontend
    ports: ["3000:3000"]
    environment:
      NEXT_PUBLIC_API_URL: http://localhost:8080
    networks: [amazonlite-network]

networks:
  amazonlite-network:
    driver: bridge
```

---

### Step 10: Create the Next.js Frontend

```bash
cd FAANG-projects
npx create-next-app@latest ecommerce-frontend \
  --typescript --tailwind --app --src-dir
cd ecommerce-frontend

npm install zustand @tanstack/react-query react-hook-form @hookform/resolvers \
  zod lucide-react react-hot-toast motion next-themes \
  @radix-ui/react-dialog @radix-ui/react-dropdown-menu @radix-ui/react-toast \
  @radix-ui/react-scroll-area @radix-ui/react-select @radix-ui/react-separator \
  @radix-ui/react-slot class-variance-authority clsx tailwind-merge \
  tailwindcss-animate embla-carousel-react recharts date-fns \
  react-medium-image-zoom geist

npm install -D @playwright/test
```

**Create files in this order:**
1. `src/types/index.ts` — all interfaces (User, Book, Cart, CartItem, Order, etc.)
2. `src/constants/index.ts` — `ROUTES`, `ENDPOINTS`, `BOOK_CATEGORIES`, `STORAGE_KEYS`
3. `src/lib/utils.ts` — `cn()` (clsx + tailwind-merge), `formatPrice()`
4. `src/lib/api-client.ts` — fetch/axios wrapper with base URL and auth header injection
5. `src/schemas/auth.schema.ts` — Zod login and register schemas
6. `src/services/auth.service.ts` — auth API methods + `decodeJwtPayload()`
7. `src/services/product.service.ts` — product API methods + `mapBook()` normalizer
8. `src/services/order.service.ts` — order API methods
9. `src/services/cart.service.ts` — cart API methods
10. `src/store/auth.store.ts` — Zustand auth store with `persist` middleware
11. `src/store/cart.store.ts` — Zustand cart store with `persist` middleware + `recalculate()`
12. `src/components/providers/QueryProvider.tsx` — TanStack Query client provider
13. `src/components/providers/AuthGuard.tsx` — redirect to /auth/login if not authenticated
14. `src/components/cart/CartDrawer.tsx` — slide-in cart drawer
15. `src/components/product/BookCard.tsx` — individual book card
16. `src/components/product/BookGrid.tsx` — responsive book grid with loading skeletons
17. `src/components/layout/Navbar.tsx` — full navbar with search, cart, auth
18. `src/components/layout/Footer.tsx` — site footer
19. `src/components/layout/HeroSection.tsx` — hero with 3D book card
20. `src/components/layout/BestsellersSection.tsx`
21. `src/components/layout/CategoriesSection.tsx`
22. `src/components/layout/StatsSection.tsx`
23. `src/components/layout/TestimonialsSection.tsx`
24. `src/components/layout/NewsletterSection.tsx`
25. `src/app/globals.css` — custom CSS classes and design tokens
26. `src/app/layout.tsx` — root layout with fonts, QueryProvider, Toaster
27. `src/app/page.tsx` — homepage composing all sections
28. `src/app/products/page.tsx` — paginated catalog with search/filter
29. `src/app/products/[id]/page.tsx` — book detail
30. `src/app/cart/page.tsx` — full cart page
31. `src/app/checkout/page.tsx` — 3-step checkout (wrapped in AuthGuard)
32. `src/app/auth/login/page.tsx` — login form
33. `src/app/auth/register/page.tsx` — register form
34. `src/app/orders/page.tsx` — order history
35. `src/app/dashboard/page.tsx` — user dashboard

**Configure `.env.local`:**
```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

**Configure `next.config.ts`** to allow external image domains for book covers.

---

### Step 11: Create Seed Scripts

```bash
cd AmazonLite
# Create seed.py, seed_docker.py, generate_books.py using psycopg2
# Connect to PostgreSQL and INSERT book records into the Books table
```

---

### Step 12: Run the Full Stack

```bash
# Backend (all services)
cd AmazonLite
docker-compose up --build -d

# Wait for Eureka registrations (~60 seconds)
# Check: http://localhost:8761

# Seed the database
python seed_docker.py

# Frontend (development)
cd ../ecommerce-frontend
npm run dev
# Open: http://localhost:3000
```

---

## 15. Testing

### Backend — JUnit Tests

Each service contains JUnit 5 tests:

| Service | Test Class | What's Tested |
|---|---|---|
| gateway-service | `GatewayApplicationTest.java` | Application context loads |
| gateway-service | `JwtValidationFilterTest.java` | Filter behavior for valid/invalid tokens |
| product-service | `ProductControllerTest.java` | Product endpoint responses |
| product-service | `AuthControllerTest.java` | Auth endpoint integration |
| notification-service | `NotificationControllerTest.java` | Controller endpoints |
| notification-service | `KafkaIntegrationTest.java` | Kafka message consumption (embedded Kafka) |
| order-service | `OrderServiceIntegrationTest.java` | Order creation flow integration |

**Run tests for a service:**
```bash
cd AmazonLite/services/auth-service
mvn test

# Run all tests
mvn clean verify
```

The notification-service uses `spring-kafka-test` with `@EmbeddedKafka` for integration tests without requiring a running Kafka broker.

### Frontend — Playwright E2E Tests

```bash
cd ecommerce-frontend

# Run headless
npm run test

# Interactive UI mode
npm run test:ui

# View HTML report
npm run test:report
```

Configured in `playwright.config.ts` to target the local Next.js dev server.

### Manual API Testing (curl)

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"reader@example.com","password":"secret123","role":"CUSTOMER"}'

# Login and capture token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"reader@example.com","password":"secret123"}' | python -c "import sys,json; print(json.load(sys.stdin)['token'])")

# Browse products (public)
curl "http://localhost:8080/api/products?page=0&size=5"

# Get featured book (public)
curl "http://localhost:8080/api/products/featured"

# Place an order (authenticated)
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 1}'

# View my orders
curl http://localhost:8080/api/orders/me \
  -H "Authorization: Bearer $TOKEN"
```

---

## Summary

| Aspect | Details |
|---|---|
| Total Microservices | 8 (Registry, Gateway, Auth, Product, Order, Notification, Inventory, Cart) |
| Backend Language | Java 17 + Spring Boot 3.2 |
| Frontend Stack | Next.js 16, React 19, TypeScript 6, Tailwind CSS v4 |
| Database | PostgreSQL 15 (shared instance), Redis (cart cache) |
| Message Broker | Apache Kafka (order.created topic) |
| Auth | JWT HS256 (JJWT 0.12.3), BCrypt, RBAC (ADMIN/SELLER/CUSTOMER) |
| Service Discovery | Netflix Eureka (lb:// routing) |
| API Gateway | Spring Cloud Gateway with global + route-specific filters |
| State Management | Zustand + persist middleware (localStorage) |
| Distributed Tracing | Correlation ID via X-Correlation-Id header + MDC |
| Frontend Testing | Playwright E2E |
| Backend Testing | JUnit 5, Spring Boot Test, spring-kafka-test (embedded) |
| Deployment | Docker Compose (11 containers on bridge network) |
| Design Theme | Premium literary bookstore — charcoal + gold palette |

## Backend CI/CD Pipeline

The backend repository implements a robust, multi-service CI/CD pipeline using GitHub Actions, leveraging `docker-compose` to seamlessly build, push, and deploy all 8 microservices simultaneously.

### Pipeline Architecture

The pipeline is split into three logical stages: **Code Quality**, **Build & Push**, and **Deploy**.

1. **Test (`test`)**:
   - Triggers on `push` and `pull_request` to `main`.
   - Iterates through all 8 Spring Boot microservices (`services/*-service`).
   - Runs `mvn test -B` for each service to ensure unit and integration tests pass.
   - _Note: If any microservice fails testing, the pipeline halts immediately._

2. **Build & Push (`build-and-push`)**:
   - Triggers only on `push` to `main` (after successful testing).
   - Authenticates to the **GitHub Container Registry (GHCR)** using the repository's `GITHUB_TOKEN`.
   - Uses `docker-compose build` and `docker-compose push` to build and upload images for all services at once.
   - Images are tagged with the exact Git commit SHA (e.g., `sha-a1b2c3d`) ensuring full traceability.

3. **Deployment (`deploy`)**:
   - Connects to the target server via SSH (`appleboy/ssh-action`).
   - Executes the `scripts/deploy.sh` script, passing the new `IMAGE_TAG` and `REGISTRY` variables.
   - The script runs `docker-compose pull` and `docker-compose up -d`. Compose intelligently detects which images have changed, stops the old containers, and spins up the new ones while leaving unmodified services (like PostgreSQL or Redis) running uninterrupted.

### Required GitHub Settings & Secrets

To make the pipeline operational, configure the following in your GitHub Repository Settings (**Settings > Secrets and variables > Actions**):

- `SERVER_HOST`: Target deployment server IP or domain.
- `SERVER_USER`: SSH username (e.g., `ubuntu` or `root`).
- `SERVER_SSH_KEY`: Private SSH key for server access.

*Ensure your repository has Write access to packages (Settings > Actions > General > Workflow permissions -> "Read and write permissions").*

### How Deployment & Rollback Work

**Deployment**: The `deploy.sh` script is completely automated. By supplying the exact image tag to `docker-compose`, it fetches the exact immutable snapshot of your microservices tied to that specific commit.

**Rollback**: Rollbacks are trivial.
1. Revert the commit in GitHub (which naturally triggers the pipeline and deploys the previous state).
2. Or manually SSH into the server and run:
   ```bash
   ./scripts/deploy.sh sha-<previous-commit-sha> ghcr.io/<owner>/amazonlite
   ```
   Docker Compose will instantly revert all 8 services to their previous versions.

### 30-Second Interview Explanation

> *"I designed a multi-service CI/CD pipeline using GitHub Actions that treats Docker Compose as a first-class citizen. Rather than managing 8 separate build pipelines, the workflow first iterates through the Spring Boot services to execute their Maven test suites. Once passing, it injects environment variables into `docker-compose.yml` to dynamically tag, build, and push all microservice images to GitHub Container Registry simultaneously. For deployment, it uses SSH to trigger a compose pull and update on the server. Compose natively handles the zero-downtime rolling restart of only the changed services. It’s a clean, declarative approach that makes multi-container rollbacks as simple as passing an older commit SHA."*

---

## Dedicated Backend CI Flow (`backend-ci.yml`)

As an alternative to the multi-service deployment above, a strictly CI-focused pipeline is provided at `.github/workflows/backend-ci.yml`. This workflow focuses solely on testing, building, and publishing the primary backend gateway image to GitHub Container Registry (GHCR).

### GitHub Actions CI Flow
- Triggers on pushes and pull requests to the main branch affecting the `services/` directory.
- Runs the complete backend test suite across all Spring Boot microservices.
- Authenticates with GHCR securely using the built-in `GITHUB_TOKEN`.
- Builds the `gateway-service` as the primary API artifact.

### GHCR Image Naming & Tags
- **Naming:** The Docker image is published exactly as the repository name (in lowercase): `ghcr.io/<github-username>/<repository-name>`.
- **`SHA` vs `latest` Tags:** Every successful build pushes two tags. The `<commit-sha>` tag acts as an immutable, uniquely identifiable version tied directly to a git commit, making rollbacks and tracking reliable. The `latest` tag is a floating pointer to the most recently built image, useful for quick local testing.

### Pulling the Image
You can find the published package by navigating to your GitHub repository's **Packages** tab (or your profile's Packages section). 

To manually pull the image locally:
```bash
# Pull the latest version
docker pull ghcr.io/<github-username>/<repository-name>:latest

# Pull a specific immutable version
docker pull ghcr.io/<github-username>/<repository-name>:<commit-sha>
```

## Deployment and Testing

This project is configured to run smoothly in a single-instance Docker Compose environment and easily scales to cloud platforms like Render.

### Architecture
- **Internal Services**: PostgreSQL (5432), Redis (6379), Auth (8082), Product (8081), Order (8083), Notification (8084), Inventory (8085), Cart (8086), and Eureka Registry (8761). These services operate within the internal docker network and should **not** be exposed publicly unless strictly required.
- **Service Discovery**: The `registry-service` acts as the internal Eureka server. Backend services use `registry-service` as their default hostname to dynamically discover one another.
- **Public API Gateway**: The `gateway-service` (8080) is the sole public entry point. It receives all external requests and routes them to the appropriate internal microservices via Eureka load balancing (e.g., `/api/products/**` to `product-service`).

### Expected Production Configuration
When deploying to cloud platforms like Render:
- The expected production Gateway URL is: `https://amazonlite-scalable-e-commerce.onrender.com`
- Set the `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` environment variable on all backend microservices to point to the live registry URL (e.g., `https://<your-registry-url>.onrender.com/eureka/`) instead of the internal default.

### Frontend API URL
The frontend application dynamically obtains its API URL via the `NEXT_PUBLIC_API_URL` environment variable.
- **Local Development**: Safely defaults to `http://localhost:8080` if no environment variable is provided.
- **Production**: Configure `NEXT_PUBLIC_API_URL=https://amazonlite-scalable-e-commerce.onrender.com` in your production environment settings.

### Verifying Service Health
Spring Boot Actuator is configured for core services. You can verify the health of an individual service by sending a GET request to its actuator health endpoint:
- **Gateway Health**: `GET https://amazonlite-scalable-e-commerce.onrender.com/actuator/health`

### Example API Request (via Gateway)
```bash
# Fetch paginated products through the public Gateway
curl -s "https://amazonlite-scalable-e-commerce.onrender.com/api/products?page=0&size=10"
```
