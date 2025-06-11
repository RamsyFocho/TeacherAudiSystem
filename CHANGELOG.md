# Changelog

All notable changes to the Teacher Audit System project will be documented in this file.

## [Unreleased]

## [1.1.0] - 2025-06-10
### Added
- **Default Admin Initialization**
  - Automatic creation of default admin user on application startup
  - Admin credentials configurable via application properties
  - Prevents duplicate admin creation on subsequent starts
  - Logs admin creation status for monitoring

### Added
- **Authentication System**
  - JWT-based authentication with refresh tokens
  - Email verification for new user registration
  - Password reset functionality
  - Role-based access control (RBAC)
  - Session management with stateless JWT

- **User Management**
  - User registration with email verification
  - User roles: ADMIN, INSPECTOR, DIRECTOR
  - User profile management
  - Account activation/deactivation

- **Security**
  - Spring Security configuration with JWT
  - Password encryption using BCrypt
  - Role-based authorization
  - CORS configuration
  - CSRF protection
  - Input validation

- **Email Service**
  - Email verification
  - Password reset emails
  - Email templates
  - Async email sending

### Changed

- **Authentication Flow**
  - Updated login to use email instead of username
  - Enhanced JWT token generation and validation
  - Improved token refresh mechanism

- **User Registration**
  - Modified registration to include role assignment
  - Added validation for user input
  - Improved error handling and messages

- **Security Configuration**
  - Updated WebSecurityConfig with role-based URL authorization
  - Added method-level security
  - Improved CORS configuration

### Fixed
- Fixed token expiration handling
- Fixed role-based access control issues
- Fixed email verification flow
- Fixed password reset functionality

## [1.0.0] - 2025-06-10
### Initial Release
- Basic project structure
- User authentication
- Role-based access control
- Email verification
- API documentation

## Security
- All passwords are hashed using BCrypt
- JWT tokens have short expiration times
- Refresh tokens can be revoked
- Rate limiting on authentication endpoints
- Input validation on all endpoints

## Dependencies
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- Java JWT
- JavaMail
- Lombok
- MapStruct
- H2 Database (for development)
- MySQL (for production)

## Configuration
Application properties are configured for:
- JWT token generation and validation
- Email service (SMTP)
- Database connection
- CORS settings
- Logging

## API Documentation
API documentation is available at `/swagger-ui.html` when running the application.

---
*This changelog follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) format.*
