# E-Commerce Backend API

## Overview

A production-style Spring Boot REST API for an e-commerce application with JWT-based authentication, role-based access control, and full order lifecycle management including cart, inventory, and discount/coupon system.

This project demonstrates backend development skills including secure authentication, layered architecture, database design, and RESTful API design.

## Features

- User registration & login
- JWT authentication (Access + Refresh Token)
- Role-based authorization (CUSTOMER, ADMIN)
- Product management (CRUD operations)
- Cart management system
- Inventory management
- Order processing system
- Discount & coupon system
- Global exception handling
- API documentation using Swagger/OpenAPI

## Tech Stack

- Java 17+
- Spring Boot
- Spring Security
- JWT (JJWT)
- Spring Data JPA (Hibernate)
- MySQL
- Spring Boot Cache (ConcurrentHashMap-based)
- OpenAPI / Swagger

## System Design Overview

- Controller Layer: Handles HTTP requests and responses
- Service Layer: Contains business logic and transactional operations
- Repository Layer: Handles database operations using Spring Data JPA
- DTO Layer: Ensures secure and clean data transfer between layers
- Security Layer: Manages authentication and authorization using JWT

## Security Features

- Stateless authentication using JWT
- Role-based access control using `@PreAuthorize`
- Spring Security filter validates every request
- Security context managed using `SecurityContextHolder`
- Refresh token mechanism for generating new access tokens
- Logout invalidates refresh token

## Security Flow

1. User registers or logs in with credentials
2. Server generates:
    - Access Token (short-lived JWT)
    - Refresh Token (UUID-based)
3. Client stores tokens securely
4. Each request passes through JWT filter
5. Token is validated and user details are loaded
6. Authentication stored in `SecurityContextHolder`
7. Role-based authorization applied using `@PreAuthorize`
8. When access token expires, refresh token is used to generate a new one
9. Logout invalidates refresh token

## Authentication & Authorization Exception Handling

- Implemented custom handling using `AuthenticationEntryPoint` and `AccessDeniedHandler`.
- `AuthenticationEntryPoint` handles unauthenticated requests (invalid/missing JWT).
- `AccessDeniedHandler` handles unauthorized access for valid users without required roles.
- Returns clear and consistent error responses to the client.

## Used .yml Over .properties 

- Provides cleaner, better structure that improves readability and manages complex configurations more efficiently