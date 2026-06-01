# EcommerceApp Backend

## Project Overview

**Application:** EcommerceApp

**Purpose:** A Spring Boot backend for an e-commerce platform that supports user registration/login, role-based access, product/catalog management, cart operations, order processing, inventory control, and coupon/discount handling.

**Tech Stack:**
- Java 17
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Security
- Spring Data JPA
- Spring Cache
- JWT via JJWT (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`)
- MySQL Connector/J
- Springdoc OpenAPI UI

**Key dependencies from `pom.xml`:**
- `spring-boot-starter-webmvc`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-cache`
- `mysql-connector-j`
- `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson`
- `org.springdoc:springdoc-openapi-starter-webmvc-ui`

## Architecture

### Package Structure

The backend code is organized under `src/main/java/org/learning/ecommerceapp` with the following domains:
- `auth` — authentication, JWT utilities, refresh tokens, security filters
- `user` — user management, registration, profile updates
- `cart` — shopping cart entities, DTOs, services, controllers
- `products` — product catalog management, DTOs, services, controllers
- `category` — product category management
- `inventory` — inventory and stock control
- `discount` — coupon management, coupon display, discount assignment
- `order` — order placement, order history, order status management
- `config` — security and JWT property configuration
- `globalException` — centralized exception handling
- `util` — helper utilities such as password encoder and current user access

### Layer Breakdown

- **Controller**: HTTP endpoints and request mapping
- **Service**: Business logic, validation, transaction coordination
- **Repository**: Spring Data JPA data access
- **Entity**: JPA entity mapping to database tables
- **DTO**: Request/response payload shapes (`dto.request`, `dto.response`)

### Domains Found

- Authentication / Authorization
- User management
- Product management
- Cart management
- Order processing
- Coupon/discount management
- Inventory management
- Category management

## Security

### JWT Authentication Flow

1. Client sends login credentials to `POST /api/authenticate`.
2. `AuthController` authenticates with `AuthenticationManager`.
3. On success, `JWTUtil` generates a JWT access token and `RefreshTokenService` creates a refresh token record.
4. Access token is returned in `AuthResDto` alongside refresh token.
5. Subsequent requests include `Authorization: Bearer <token>`.
6. `JWTAuthFilter` intercepts requests, extracts the token, validates it, and loads `UserDetails` from `UserService`.
7. Validated authentication is stored in `SecurityContextHolder`.

### JWTAuthFilter Explanation

- Extends `OncePerRequestFilter`.
- Skips filtering for:
  - `/api/authenticate`
  - `/api/createUser`
  - `/api/refreshAuth`
- Extracts bearer token from `Authorization` header.
- Validates token expiration and username.
- Loads user data from `UserService` and injects authentication into the security context.
- Responds with `401 Unauthorized` when the access token is expired.

### Protected vs Public Endpoints

**Public endpoints:**
- `/api/authenticate`
- `/api/createUser`
- `/api/refreshAuth`
- Static assets and HTML paths used by the app

**Protected endpoints:** All other API endpoints require authentication.

### Role-Based Access

Role checks are enforced with `@PreAuthorize` annotations.

- `ROLE_CUSTOMER`:
  - Cart management
  - Viewing products/categories
  - Placing orders
  - Viewing and using coupons
  - Updating own profile
  - Order history retrieval
- `ROLE_ADMIN`:
  - Product CRUD
  - Category CRUD
  - Inventory updates
  - Coupon assignment and coupon listing
  - Viewing all users and all orders
  - Order lifecycle updates

## API Endpoints

### Authentication

- `POST /api/authenticate`
  - Public
  - Authenticate user and return access + refresh tokens
- `POST /api/createUser`
  - Public
  - Register a new user and return tokens
- `POST /api/refreshAuth`
  - Public
  - Refresh access token using refresh token
- `POST /api/deleteRefreshAuth`
  - Authenticated
  - Revoke a refresh token

### Cart

- `GET /api/cart/getCart`
  - Role: CUSTOMER
  - Retrieve current user cart details
- `POST /api/cart/addToCart`
  - Role: CUSTOMER
  - Add item to cart
- `DELETE /api/cart/delete/{productId}`
  - Role: CUSTOMER
  - Remove a cart item
- `PATCH /api/cart/update`
  - Role: CUSTOMER
  - Update item quantity in cart
- `DELETE /api/cart/deleteAll`
  - Role: CUSTOMER
  - Remove all items from cart

### Category

- `POST /api/categories/addCategory/{categoryName}`
  - Role: ADMIN
  - Create a new product category
- `PATCH /api/categories/updateCategoryName/{categoryId}`
  - Role: ADMIN
  - Rename a category
- `GET /api/categories/getCategories`
  - Role: CUSTOMER, ADMIN
  - List all categories

### Product

- `POST /api/products/addProducts`
  - Role: ADMIN
  - Bulk add products
- `GET /api/products/listAllProducts`
  - Role: CUSTOMER, ADMIN
  - List products with pagination
- `GET /api/products/listAllProductsForAdmin`
  - Role: CUSTOMER, ADMIN
  - List products for admin view
- `GET /api/products/listProductByCategory/{categoryId}`
  - Role: CUSTOMER, ADMIN
  - List products by category
- `DELETE /api/products/deleteProduct/{productId}`
  - Role: ADMIN
  - Delete a product
- `GET /api/products/getProduct/{productId}`
  - Role: CUSTOMER, ADMIN
  - Get product details
- `PATCH /api/products/updateProduct/price/{productId}`
  - Role: ADMIN
  - Update product price
- `PATCH /api/products/updateProduct/name/{productId}`
  - Role: ADMIN
  - Update product name
- `PATCH /api/products/updateProduct/description/{productId}`
  - Role: ADMIN
  - Update product description
- `PATCH /api/products/updateProduct/category/{productId}/{categoryId}`
  - Role: ADMIN
  - Change product category
- `GET /api/products/filterProducts`
  - Role: CUSTOMER, ADMIN
  - Filter products by price range
- `GET /api/products/sortByPrice/{asc}`
  - Role: CUSTOMER, ADMIN
  - Sort products by price
- `GET /api/products/sortByName/{asc}`
  - Role: CUSTOMER, ADMIN
  - Sort products by name
- `GET /api/products/getInStockProducts`
  - Role: CUSTOMER, ADMIN
  - List products currently in stock
- `GET /api/products/findByMatchingName/{matchingCase}`
  - Role: CUSTOMER, ADMIN
  - Search products by name fragment

### Discount / Coupon

- `POST /api/coupon/addCoupons/{filterPrice}`
  - Role: ADMIN
  - Assign coupons to eligible users based on filter price
- `POST /api/coupon/addCouponsToAllUsers`
  - Role: ADMIN
  - Assign coupons to all users
- `GET /api/coupon/displayCoupons`
  - Role: CUSTOMER
  - Display coupons available to the current user
- `GET /api/coupon/displayAllCoupons`
  - Role: ADMIN
  - Display all coupons

### Inventory

- `PATCH /api/inventory/updateInventory/{productId}`
  - Role: ADMIN
  - Update inventory quantity for a product

### User

- `GET /api/users/getUsers`
  - Role: ADMIN
  - List all users
- `GET /api/users/getUser`
  - Role: CUSTOMER, ADMIN
  - Get current logged-in user profile
- `PATCH /api/users/password`
  - Role: CUSTOMER, ADMIN
  - Change user password
- `PATCH /api/users/contactNo`
  - Role: CUSTOMER, ADMIN
  - Update user contact number
- `PATCH /api/users/address`
  - Role: CUSTOMER, ADMIN
  - Add or update user address
- `PATCH /api/users/deleteUser`
  - Role: CUSTOMER, ADMIN
  - Delete the current user account

### Order

- `POST /api/orders/placeOrder`
  - Role: CUSTOMER
  - Place a new order
- `GET /api/orders/getOrder/{orderNumber}`
  - Role: CUSTOMER
  - Retrieve details for a specific order
- `GET /api/orders/getOrders`
  - Role: CUSTOMER
  - Retrieve current user order history
- `GET /api/orders/getAllOrders`
  - Role: ADMIN
  - Retrieve all orders in the system
- `PATCH /api/orders/updateOrder/pending/{orderNumber}`
  - Role: ADMIN
  - Set order status to PENDING
- `PATCH /api/orders/updateOrder/confirmed/{orderNumber}`
  - Role: ADMIN
  - Set order status to CONFIRMED
- `PATCH /api/orders/updateOrder/processing/{orderNumber}`
  - Role: ADMIN
  - Set order status to PROCESSING
- `PATCH /api/orders/updateOrder/shipped/{orderNumber}`
  - Role: ADMIN
  - Set order status to SHIPPED
- `PATCH /api/orders/updateOrder/outForDelivery/{orderNumber}`
  - Role: ADMIN
  - Set order status to OUT FOR DELIVERY
- `PATCH /api/orders/updateOrder/delivered/{orderNumber}`
  - Role: ADMIN
  - Set order status to DELIVERED
- `PATCH /api/orders/cancelOrder/{orderNumber}`
  - Role: CUSTOMER
  - Cancel a user order

## Database Model

### Core Entities

- `Users`
  - `@Entity`, table `users`
  - Fields: id, name, userName, emailId, contactNo, password, role, status, createdAt
  - Relationships:
    - `@OneToMany(cascade = CascadeType.ALL)` to `Address`
    - `@OneToOne(mappedBy = "users", cascade = CascadeType.ALL)` to `Cart`
    - `@OneToMany(mappedBy = "users", cascade = CascadeType.ALL)` to `Orders`
    - `@OneToMany(mappedBy = "users", cascade = CascadeType.ALL)` to `DiscountOnUsers`

- `Address`
  - `@Entity`
  - Stores user address fields with `isDefault`

- `Cart`
  - `@Entity`
  - `@OneToOne` to `Users`
  - `@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)` to `CartItems`

- `CartItems`
  - `@Entity`
  - `@ManyToOne` to `Cart`
  - `@ManyToOne` to `Products`
  - Quantity validation with `@Min(1)`

- `Products`
  - `@Entity`, table `products`
  - `@ManyToOne` to `ProductCategory`
  - `@OneToOne(mappedBy = "products", cascade = CascadeType.ALL)` to `Inventory`
  - `@OneToMany(mappedBy = "products", cascade = CascadeType.ALL)` to `OrderItems`

- `ProductCategory`
  - `@Entity`
  - Category name management

- `Inventory`
  - `@Entity`
  - `@OneToOne` to `Products`
  - Uses `@Version` for optimistic locking

- `DiscountOnUsers`
  - `@Entity`
  - `@ManyToOne` to `Users`
  - Stores coupon metadata, date range, usage limits, active flag, and discount values

- `GlobalDiscountOnProducts`
  - `@Entity`
  - Stores global coupon metadata for products

- `Orders`
  - `@Entity`, table `orders`
  - `@ManyToOne` to `Users`
  - `@OneToMany(mappedBy = "orders", cascade = CascadeType.ALL)` to `OrderItems`
  - Tracks orderNumber, orderDate, orderStatus, finalPrice, appliedCoupon

- `OrderItems`
  - `@Entity`, table `order_items`
  - `@ManyToOne` to `Products`
  - `@ManyToOne(cascade = CascadeType.ALL)` to `Orders`
  - Tracks quantity, sellingPrice, discount, totalPrice, deliveryCharge

- `RefreshToken`
  - `@Entity`, table `refresh_tokens`
  - Stores token, expiryDate, revoked, username, role, device info, ip address

### Notable JPA / Cascade Rules

- Multiple entities use `CascadeType.ALL` for owner-driven parent-child persistence.
- `Cart`, `Users`, `Orders`, and `Products` cascade child entities to simplify lifecycle management.
- `Inventory` uses `@Version` for optimistic locking to avoid concurrent stock update conflicts.

## Caching

Caching is enabled via `@EnableCaching` in `EcommerceApplication`.

### Cached components

- `CartCacheService`
  - `@Cacheable(value = "cart", key = "#loggedUser")`
  - Caches the cart response per logged-in user
  - `@CacheEvict` on cart modifications ensures stale cart state is removed

- `ProductService`
  - `@Cacheable(value = "products", key = "#productId")`
  - Caches individual product fetches by ID
  - `@CacheEvict` on product delete operations

## Key Design Decisions

- **Layered architecture**: Controllers delegate to services, services encapsulate business logic, repositories handle persistence.
- **DTO usage**: Request and response DTOs are used for authentication, product operations, cart updates, orders, and coupons.
- **Centralized exception handling**: `GlobalExceptionHandler` converts domain exceptions into HTTP responses.
- **JWT security**: Access tokens plus refresh tokens support stateless session management.
- **Cache layer**: Cart and product caches improve read performance and avoid repeated database queries.
- **Versioned inventory**: `@Version` protects inventory updates from race conditions.

## How to Run

### Prerequisites

- Java 17 JDK
- Maven 3.x
- MySQL database
- Network access for dependencies

### Configuration

Update `src/main/resources/application.yml` as needed.

Sample database configuration:

```yaml
server:
  port: 9096

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ecommerce
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: false

jwt:
  access-token-expiration: 300000
  refresh-token-expiration: 10
  secret: <your-secret-key>
```

### Run steps

1. Create the MySQL database named `ecommerce`.
2. Configure credentials and JWT secret in `application.yml`.
3. Build the project:

```bash
mvn clean package
```

4. Run the application:

```bash
mvn spring-boot:run
```

Or run the generated JAR:

```bash
java -jar target/ecommerceapp-1.0-SNAPSHOT.jar
```

### Notes

- The app uses `spring.jpa.hibernate.ddl-auto=update`, so schema changes are auto-applied.
- Swagger/OpenAPI UI is available via Springdoc if enabled by default.
