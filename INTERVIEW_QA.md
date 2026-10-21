# 🎤 Technical Interview Q&A - Hospital Management System

This document contains common technical questions asked during interviews based on this project, along with detailed answers and architectural justifications.

---

### 1. Walk me through your HMS architecture.
**Answer:** The system follows an N-Tier architecture. The frontend is built with Thymeleaf and Vanilla JS. When a user interacts, a fetch call goes to the Spring Boot Controllers. The controllers use `@PreAuthorize` for RBAC, then pass the request to the Service layer where the business logic lives. The Service layer uses Repositories (Spring Data JPA) to talk to PostgreSQL. For security, a custom `JwtAuthenticationFilter` intercepts requests before they reach the controllers.

### 2. How does Spring Security work in your project? (From login to JWT)
**Answer:** When the user hits `/api/auth/login`, Spring's `AuthenticationManager` verifies the username and BCrypt password against the database. If successful, my `JwtService` generates a JWT containing the username and roles. This token is returned to the client. For subsequent requests, the client sends the token in the `Authorization: Bearer <token>` header. My `JwtAuthenticationFilter` intercepts the request, validates the token, and sets the `Authentication` in the SecurityContext.

### 3. What is the difference between `@Controller` and `@RestController`? Why did you use both?
**Answer:** `@RestController` is a convenience annotation that combines `@Controller` and `@ResponseBody`. I used `@RestController` for all my `/api/` endpoints because they return JSON data. I used `@Controller` for my `PageController` because it returns Thymeleaf view names (e.g., `return "login";`) rather than raw data.

### 4. What is the DTO pattern and why did you use it?
**Answer:** DTO stands for Data Transfer Object. I used them to prevent exposing my database entities directly to the API. If I exposed my `Doctor` entity, it would trigger infinite recursion because Doctor maps to Department, which maps back to Doctor. DTOs also allow me to hide sensitive data (like hashed passwords) and only send exactly the fields the frontend needs.

### 5. What is the difference between `@PreAuthorize` and `SecurityConfig` rules?
**Answer:** `SecurityConfig` handles URL-level security (e.g., `anyRequest().authenticated()`). But `@PreAuthorize` handles method-level security. It's more granular. For example, on my `createPatient` method, I have `@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")`. This ensures even if the URL is accessible, the specific method checks the user's role.

### 6. How does your double-booking prevention work?
**Answer:** In my `AppointmentService`, before creating an appointment, I query the database using a custom repository method `findByDoctorIdAndAppointmentDate`. If an appointment exists for that doctor on that date, I check if the `timeSlot` matches. If it does (and the status is not CANCELLED), I throw a `RuntimeException` which gets caught by my GlobalExceptionHandler and returns a 400 Bad Request.

### 7. What is `LazyInitializationException` and how did you handle it?
**Answer:** It happens when Hibernate tries to load a related entity (like a Doctor's Department) after the database session has been closed. I handled it by setting `FetchType.EAGER` on my `@ManyToOne` relationships. However, I am aware that this is a small-scale workaround. At scale, EAGER fetch causes the N+1 query problem. The proper enterprise fix would be to use `@Transactional` on the service methods or use Entity Graphs to fetch exactly what is needed.

### 8. What is `@RestControllerAdvice` and how does your `GlobalExceptionHandler` work?
**Answer:** It's a global interceptor for exceptions. Instead of writing try-catch blocks in every controller, my `GlobalExceptionHandler` catches specific exceptions like `ResourceNotFoundException` or `AccessDeniedException` and formats them into a consistent JSON `ErrorResponse` with the correct HTTP status code (like 404 or 403).

### 9. What is stateless authentication and why is it better for REST APIs?
**Answer:** It means the server does not store any session data. Each request must contain all the information needed to authenticate it (the JWT). This is better for REST APIs because it makes the backend highly scalable. I can spin up 5 servers, and any of them can validate the JWT without needing a shared session cache.

### 10. How would you scale this application for 10,000 simultaneous users?
**Answer:** 
1. **Database:** Fix the EAGER fetch issue using `@Transactional` and Pagination. Add database indexing on frequent query columns.
2. **Caching:** Add Redis caching for read-heavy endpoints.
3. **Frontend:** Decouple the Thymeleaf frontend and build a React frontend served via a CDN.
4. **Infrastructure:** Containerize the Spring Boot app with Docker and deploy behind a Load Balancer (like AWS ALB).