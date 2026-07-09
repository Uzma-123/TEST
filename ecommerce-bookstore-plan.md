# eCommerce Bookstore Backend — Plan

## Top-Level Overview

Build a RESTful backend for an eCommerce bookstore using **Java + Spring (Spring Boot 3.x)** deployed on **IBM WebSphere Liberty Server**. The database is **MySQL**. The architecture follows a strict layered pattern:

```
Controller → Manager → Transaction → DAO
```

- **Controller** — handles HTTP request/response, input validation, maps to/from DTOs
- **Manager** — orchestrates business logic, calls one or more Transactions
- **Transaction** — handles a single unit of transactional work, calls DAOs
- **DAO** — data access layer, interacts with MySQL via Spring Data JPA / JPQL

Authentication uses **JWT (JSON Web Tokens)**. Payments are simulated internally (no external gateway). The project is built with **Maven** and packaged as a **WAR** for Liberty deployment.

---

## Architecture Diagram

```
Client
  │
  ▼
[Controller Layer]   ← DTOs, input validation, HTTP mapping
  │
  ▼
[Manager Layer]      ← Business logic, orchestration
  │
  ▼
[Transaction Layer]  ← @Transactional units of work
  │
  ▼
[DAO Layer]          ← Spring Data JPA Repositories / JPQL
  │
  ▼
[MySQL Database]
```

---

## Sub-Tasks

---

### Sub-Task 1 — Project Scaffolding & Liberty Configuration

**Intent**
Set up the Maven project structure, Spring Boot 3.x dependencies, WAR packaging for Liberty, and MySQL datasource configuration.

**Expected Outcomes**
- Maven `pom.xml` with Spring Boot, Spring Security, Spring Data JPA, MySQL driver, JWT library, and Liberty plugin dependencies
- `src/main/liberty/config/server.xml` configured with HTTP endpoint, datasource, and application reference
- `application.properties` with MySQL connection, JPA settings, and JWT secret placeholder
- Base package structure: `com.bookstore` with sub-packages `controller`, `manager`, `transaction`, `dao`, `model`, `dto`, `config`, `exception`
- App starts on Liberty and connects to MySQL

**Todo List**
1. Create Maven project with `pom.xml` — set packaging to `war`, add Spring Boot 3.x parent, Spring Web, Spring Security, Spring Data JPA, MySQL Connector, JJWT (JWT library), and `liberty-maven-plugin`
2. Add `SpringBootServletInitializer` subclass for WAR deployment
3. Create `server.xml` for Liberty — define `webApplication`, `dataSource` (MySQL JDBC), and required Liberty features (`servlet-6.0`, `jpa-3.0`, `jdbc-4.2`)
4. Create `application.properties` — MySQL URL/credentials, JPA dialect, DDL auto, JWT secret and expiry
5. Create the base package directory structure
6. Create a global exception handler (`GlobalExceptionHandler`) using `@RestControllerAdvice`
7. Create a standard `ApiResponse<T>` wrapper DTO for all responses

**Relevant Context**
- Liberty WAR deployment requires `SpringBootServletInitializer` and `war` packaging
- Liberty `server.xml` must declare `dataSource` with `jdbcDriver` pointing to MySQL connector
- Use `liberty-maven-plugin` (`io.openliberty.tools`) for `mvn liberty:run`

**Status:** `[x] done`

---

### Sub-Task 2 — Domain Model & Database Schema

**Intent**
Define all JPA entity classes that map to the MySQL schema, covering all core domains.

**Expected Outcomes**
- Entity classes created: `User`, `Role`, `Book`, `Category`, `CartItem`, `Order`, `OrderItem`, `Payment`, `Review`
- Relationships correctly mapped (OneToMany, ManyToOne, ManyToMany)
- MySQL tables auto-created via `spring.jpa.hibernate.ddl-auto=update`

**Todo List**
1. Create `Role` entity — fields: `id`, `name` (enum: USER, ADMIN)
2. Create `User` entity — fields: `id`, `firstName`, `lastName`, `email`, `password`, `createdAt`; ManyToMany with `Role`
3. Create `Category` entity — fields: `id`, `name`, `description`
4. Create `Book` entity — fields: `id`, `title`, `author`, `isbn`, `description`, `price`, `stockQuantity`, `imageUrl`, `createdAt`; ManyToOne with `Category`
5. Create `CartItem` entity — fields: `id`, `quantity`; ManyToOne with `User` and `Book`
6. Create `Order` entity — fields: `id`, `orderDate`, `status` (enum: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED), `totalAmount`, `shippingAddress`; ManyToOne with `User`; OneToMany with `OrderItem`
7. Create `OrderItem` entity — fields: `id`, `quantity`, `unitPrice`; ManyToOne with `Order` and `Book`
8. Create `Payment` entity — fields: `id`, `amount`, `status` (enum: PENDING, COMPLETED, FAILED, REFUNDED), `paymentMethod`, `transactionReference`, `paidAt`; OneToOne with `Order`
9. Create `Review` entity — fields: `id`, `rating`, `comment`, `createdAt`; ManyToOne with `User` and `Book`

**Relevant Context**
- All entities in `com.bookstore.model`
- Use Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) to reduce boilerplate
- Use `@CreationTimestamp` for audit date fields

**Status:** `[x] done`

---

### Sub-Task 3 — DAO Layer

**Intent**
Create Spring Data JPA repository interfaces for each entity, with custom JPQL queries where needed.

**Expected Outcomes**
- Repository interface for every entity
- Custom query methods for non-trivial lookups (e.g., find books by category, find cart items by user, find orders by user)

**Todo List**
1. Create `UserDAO` — `findByEmail`, `existsByEmail`
2. Create `RoleDAO` — `findByName`
3. Create `BookDAO` — `findByCategoryId`, `findByTitleContainingIgnoreCase`, `findByAuthorContainingIgnoreCase`
4. Create `CategoryDAO` — standard CRUD
5. Create `CartItemDAO` — `findByUserId`, `findByUserIdAndBookId`, `deleteByUserId`
6. Create `OrderDAO` — `findByUserId`, `findByUserIdAndStatus`
7. Create `OrderItemDAO` — `findByOrderId`
8. Create `PaymentDAO` — `findByOrderId`
9. Create `ReviewDAO` — `findByBookId`, `findByUserId`, `existsByUserIdAndBookId`

**Relevant Context**
- All DAOs in `com.bookstore.dao`
- Extend `JpaRepository<Entity, Long>`
- Use `@Query` annotation for JPQL where method name derivation is complex

**Status:** `[x] done`

---

### Sub-Task 4 — Security & JWT Authentication

**Intent**
Implement user registration, login, and JWT-based stateless authentication with Spring Security.

**Expected Outcomes**
- `POST /api/auth/register` — registers a new user, returns JWT
- `POST /api/auth/login` — authenticates user, returns JWT
- All protected endpoints require `Authorization: Bearer <token>` header
- Roles (USER, ADMIN) enforced via `@PreAuthorize`

**Todo List**
1. Create `JwtUtil` config class — generate, validate, and extract claims from JWT using JJWT
2. Create `JwtAuthenticationFilter` — extends `OncePerRequestFilter`, validates token and sets `SecurityContext`
3. Create `SecurityConfig` — `@Configuration`, `@EnableWebSecurity`, `@EnableMethodSecurity`; configure `SecurityFilterChain` to permit `/api/auth/**`, require auth for all others; add `JwtAuthenticationFilter`
4. Create `AuthDTO` classes — `RegisterRequest`, `LoginRequest`, `AuthResponse` (contains JWT token and user info)
5. Create `AuthDAO` — delegates to `UserDAO`
6. Create `AuthTransaction` — `registerUser` (encode password, assign USER role, save), `loginUser` (authenticate credentials, return user)
7. Create `AuthManager` — calls `AuthTransaction`, generates JWT via `JwtUtil`, returns `AuthResponse`
8. Create `AuthController` — `POST /api/auth/register`, `POST /api/auth/login`

**Relevant Context**
- All security config in `com.bookstore.config`
- Use `BCryptPasswordEncoder` for password hashing
- `UserDetailsService` implementation loads user by email from `UserDAO`

**Status:** `[x] done`

---

### Sub-Task 5 — Book Catalog Module

**Intent**
Implement full CRUD for books and categories, accessible by admins, readable by all.

**Expected Outcomes**
- `GET /api/books` — list all books (public), supports query params: `category`, `search`
- `GET /api/books/{id}` — get single book (public)
- `POST /api/books` — create book (ADMIN only)
- `PUT /api/books/{id}` — update book (ADMIN only)
- `DELETE /api/books/{id}` — delete book (ADMIN only)
- `GET /api/categories` — list categories (public)
- `POST /api/categories` — create category (ADMIN only)

**Todo List**
1. Create `BookDTO`, `BookRequest`, `CategoryDTO`, `CategoryRequest` DTO classes
2. Create `BookTransaction` — `createBook`, `updateBook`, `deleteBook`, `getBookById`, `getAllBooks`, `getBooksByCategory`, `searchBooks`
3. Create `CategoryTransaction` — `createCategory`, `getAllCategories`
4. Create `BookManager` — orchestrates book and category transactions, maps entities to DTOs
5. Create `BookController` — maps all book endpoints; use `@PreAuthorize("hasRole('ADMIN')")` on write operations
6. Create `CategoryController` — maps all category endpoints

**Relevant Context**
- All classes in respective `com.bookstore.*` sub-packages
- `BookTransaction` and `CategoryTransaction` are both `@Transactional`
- Stock quantity must be checked before allowing add-to-cart (enforced in Cart module)

**Status:** `[x] done`

---

### Sub-Task 6 — Shopping Cart Module

**Intent**
Allow authenticated users to manage their shopping cart.

**Expected Outcomes**
- `GET /api/cart` — get current user's cart items
- `POST /api/cart` — add a book to cart (or increment quantity)
- `PUT /api/cart/{cartItemId}` — update quantity of a cart item
- `DELETE /api/cart/{cartItemId}` — remove item from cart
- `DELETE /api/cart` — clear entire cart

**Todo List**
1. Create `CartItemDTO`, `AddToCartRequest`, `UpdateCartRequest` DTO classes
2. Create `CartTransaction` — `getCartByUser`, `addToCart` (check stock, upsert), `updateCartItem`, `removeCartItem`, `clearCart`
3. Create `CartManager` — calls `CartTransaction`, resolves current user from security context, maps to DTOs
4. Create `CartController` — maps all cart endpoints; all require authenticated USER

**Relevant Context**
- Current user resolved from `SecurityContextHolder` in Manager layer
- Stock check: if requested quantity > `Book.stockQuantity`, throw `InsufficientStockException`

**Status:** `[x] done`

---

### Sub-Task 7 — Orders Module

**Intent**
Allow users to place orders from their cart, view order history, and allow admins to manage order status.

**Expected Outcomes**
- `POST /api/orders` — place order from current cart (clears cart after)
- `GET /api/orders` — get current user's orders
- `GET /api/orders/{id}` — get order detail
- `PUT /api/orders/{id}/status` — update order status (ADMIN only)
- `DELETE /api/orders/{id}` — cancel order (USER, only if PENDING)

**Todo List**
1. Create `OrderDTO`, `OrderItemDTO`, `PlaceOrderRequest` (shipping address), `UpdateOrderStatusRequest` DTO classes
2. Create `OrderTransaction` — `placeOrder` (snapshot cart into OrderItems, decrement stock, clear cart), `getOrdersByUser`, `getOrderById`, `updateOrderStatus`, `cancelOrder`
3. Create `OrderManager` — calls `OrderTransaction`, resolves current user, maps to DTOs
4. Create `OrderController` — maps all order endpoints with appropriate role guards

**Relevant Context**
- `placeOrder` must be `@Transactional` — stock decrement + cart clear + order creation must be atomic
- Price is snapshotted at time of order (`OrderItem.unitPrice = Book.price` at order time)

**Status:** `[x] done`

---

### Sub-Task 8 — Payments Module

**Intent**
Simulate payment processing for a placed order and record the payment status.

**Expected Outcomes**
- `POST /api/payments` — initiate payment for an order (simulated: always succeeds unless order already paid)
- `GET /api/payments/{orderId}` — get payment details for an order
- Payment status recorded on `Payment` entity linked to the `Order`

**Todo List**
1. Create `PaymentDTO`, `PaymentRequest` (orderId, paymentMethod) DTO classes
2. Create `PaymentTransaction` — `processPayment` (create Payment record, set status COMPLETED, update Order status to CONFIRMED), `getPaymentByOrderId`
3. Create `PaymentManager` — validates order belongs to current user, calls `PaymentTransaction`, generates a mock `transactionReference`
4. Create `PaymentController` — maps payment endpoints

**Relevant Context**
- Simulated payment: no external API call; generate a UUID as `transactionReference`
- Guard: user can only pay for their own orders
- If order already has a COMPLETED payment, throw `PaymentAlreadyProcessedException`

**Status:** `[x] done`

---

### Sub-Task 9 — Reviews Module

**Intent**
Allow authenticated users to leave reviews on books they have purchased.

**Expected Outcomes**
- `GET /api/books/{bookId}/reviews` — get all reviews for a book (public)
- `POST /api/books/{bookId}/reviews` — submit a review (USER, must have purchased the book)
- `DELETE /api/books/{bookId}/reviews/{reviewId}` — delete a review (owner or ADMIN)

**Todo List**
1. Create `ReviewDTO`, `ReviewRequest` (rating 1–5, comment) DTO classes
2. Create `ReviewTransaction` — `getReviewsByBook`, `addReview` (check purchase, check no duplicate review), `deleteReview`
3. Create `ReviewManager` — resolves current user, calls `ReviewTransaction`, maps to DTOs
4. Create `ReviewController` — maps review endpoints under `/api/books/{bookId}/reviews`

**Relevant Context**
- Purchase check: user must have at least one DELIVERED order containing the book
- Duplicate check: `ReviewDAO.existsByUserIdAndBookId` — one review per user per book
- Rating must be validated: 1 ≤ rating ≤ 5

**Status:** `[x] done`

---

## Cross-Cutting Concerns

| Concern | Approach |
|---|---|
| Exception Handling | `GlobalExceptionHandler` (`@RestControllerAdvice`) maps exceptions to structured error responses |
| Response Wrapping | All endpoints return `ApiResponse<T>` with `success`, `message`, `data` fields |
| Validation | `@Valid` + Bean Validation (`@NotBlank`, `@Min`, `@Max`, etc.) on all request DTOs |
| Authorization | `@PreAuthorize` on controller methods; current user from `SecurityContextHolder` |
| Transactions | `@Transactional` on all Transaction-layer classes |
| Logging | SLF4J + Logback (Liberty compatible) |
