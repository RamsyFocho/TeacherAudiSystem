# Teacher Report System API Documentation

## Table of Contents
1. [Authentication](#authentication)
   - [Login](#login)
   - [Register](#register)
   - [Verify Email](#verify-email)
   - [Resend Verification Email](#resend-verification-email)
   - [Refresh Token](#refresh-token)

2. [Reports](#reports)
   - [Create Report](#create-report)
   - [Get All Reports](#get-all-reports)
   - [Get Report by ID](#get-report-by-id)
   - [Update Report](#update-report)
   - [Delete Report](#delete-report)
   - [Search Reports](#search-reports)

3. [Teachers](#teachers)
   - [Upload Teachers](#upload-teachers)

4. [Establishments](#establishments)
   - [Get All Establishments](#get-all-establishments)
   - [Create Establishment](#create-establishment)
   - [Create Multiple Establishments](#create-multiple-establishments)

5. [Email Messaging](#email-messaging)
6. [Security](#security)
7. [Error Handling](#error-handling)
8. [User Roles & Permissions](#user-roles--permissions)

---

## User Roles & Permissions

| Role           | Description                                              |
| -------------- | -------------------------------------------------------- |
| ROLE_ADMIN     | Full access to all endpoints, user and data management   |
| ROLE_INSPECTOR | Create/view reports, inspections      |
| ROLE_DIRECTOR  | View reports, analytics/dashboard, manage establishments|

**Endpoint Access Summary:**

| Endpoint Pattern                | Roles Allowed                      |
| -------------------------------| -----------------------------------|
| /api/auth/**                   | All (public)                       |
| /api/admin/**, /api/users/**   | ROLE_ADMIN                         |
| /api/teachers/**               | ROLE_ADMIN, ROLE_DIRECTOR         |
| /api/inspections/**            | ROLE_INSPECTOR, ROLE_DIRECTOR       |
| /api/reports/**                | All authenticated                   |
| /api/analytics/**, /api/dashboard/** | ROLE_ADMIN, ROLE_DIRECTOR     |
| /api/establishments/**         | ROLE_ADMIN, ROLE_DIRECTOR           |
| Others                         | Authenticated users                 |

---

## Authentication

### Login
Authenticate a user and retrieve JWT tokens.

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "roles": ["ROLE_USER"]
}
```

### Register
Register a new user (Admin only).

**Endpoint:** `POST /api/auth/register`

**Headers:**
- `Authorization: Bearer <admin_token>`

**Request Body:**
```json
{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123",
  "phoneNumber": "+1234567890",
  "address": "123 Main St",
  "role": "ROLE_INSPECTOR"
}
```

### Verify Email
Verify a user's email address using the token sent to their email.

- `token`: The verification token
- `redirect` (optional): URL to redirect to after verification

- `email`: User's email address

**Response:**
```json
{
  "message": "A new verification email has been sent to user@example.com. Please check your inbox and follow the instructions to verify your account."
}
```

### Refresh Token
Get a new access token using a refresh token.

**Endpoint:** `POST /api/auth/refreshToken`

**Request Body:**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```
---

## Reports

### Create Report
Create a new report.

**Endpoint:** `POST /api/reports`

**Headers:**
- `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "establishment": {
    "name": "Example High School"
  },
  "teacher": {
     "firstName": "John",
    "lastName": "Doe"
  },
  "className": "10A",
  "courseTitle": "Mathematics",
  "date": "2025-06-12",
  "startTime": "08:00:00",
  "endTime": "09:30:00",
  "studentPresent": 2,
  "observation": "Class was well prepared and engaging.",
  "sanctionType": "NONE"
}
```

**Response:**
```json
{
  "message": "Report created successfully"
}

### Get All Reports

- `Authorization: Bearer <token>`

**Response:**
    "establishment": {"name": "Example High School"},
    "className": "10A",
    "courseTitle": "Mathematics",
    "date": "2025-06-12",
    "absentStudents": 2,
    "observation": "Class was well prepared and engaging.",
    "sanctionType": "NONE"
  }
]
```

### Get Report by ID
Get a specific report by its ID.

**Endpoint:** `GET /api/reports/{id}`

**Headers:**
- `Authorization: Bearer <token>`

**Response:**
```json
{
  "id": 1,
  "establishment": {"name": "Example High School"},
  "teacher": {"firstName": "John", "lastName": "Doe"},
  "className": "10A",
  "courseTitle": "Mathematics",
  "date": "2025-06-12",
  "startTime": "08:00:00",
  "endTime": "09:30:00",
  "presentStudents": 25,
  "absentStudents": 2,
  "observation": "Class was well prepared and engaging.",
  "sanctionType": "NONE"
}
```

### Update Report
Update an existing report (Admin only).

**Endpoint:** `PUT /api/reports/sanction/{id}`

**Headers:**
- `Authorization: Bearer <admin_token>`

**Request Body:**
```json
{
  "id": 1,
  "sanctionType": "WARNING"
}
```

**Response:**
```json
{
  "id": 1,
  "sanctionType": "WARNING"
  // ... other fields
}
```

### Delete Report
Delete a report (Admin only).

**Endpoint:** `DELETE /api/reports/{id}`

**Headers:**
- `Authorization: Bearer <admin_token>`

**Response:**
- `204 No Content` on success

### Search Reports
Search reports by various criteria.

**Endpoints:**
- `GET /api/reports/search/teacher?name=John` - Search by teacher name
- `GET /api/reports/search/establishment?name=High` - Search by establishment name
- `GET /api/reports/search/class?name=10A` - Search by class name
- `GET /api/reports/search/course?title=Math` - Search by course title
- `GET /api/reports/date-range?startDate=2025-01-01T00:00:00Z&endDate=2025-12-31T23:59:59Z` - Search by date range
- `GET /api/reports/search/description?keyword=engaging` - Search in descriptions
- `GET /api/reports/search/year/2025` - Search by year

---

## Teachers

### Upload Teachers
Upload teachers from an Excel file (Admin only).

**Endpoint:** `POST /api/teachers/upload`

**Headers:**
- `Authorization: Bearer <admin_token>`
- `Content-Type: multipart/form-data`

**Request Body:**
- `file`: Excel file containing teacher data

**Response:**
```json
{
  "message": "Teachers uploaded successfully."
}
```

---

## Establishments

### Get All Establishments
Get a list of all establishments.

**Endpoint:** `GET /api/establishments`

**Headers:**
- `Authorization: Bearer <token>`

**Response:**
```json
[
  {"id": 1, "name": "Example High School"},
  {"id": 2, "name": "Another School"}
]
```

### Create Establishment
Create a new establishment (Admin only).

**Endpoint:** `POST /api/establishments`

**Headers:**
- `Authorization: Bearer <admin_token>`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "name": "New School"
}
```

**Response:**
```json
{
  "id": 3,
  "name": "New School"
}
```

### Create Multiple Establishments
Create multiple establishments at once (Admin only).

**Endpoint:** `POST /api/establishments/list`

**Headers:**
- `Authorization: Bearer <admin_token>`
- `Content-Type: application/json`

**Request Body:**
```json
[
  {"name": "School A"},
  {"name": "School B"}
]
```

**Response:**
```json
{
  "message": "Establishments created successfully"
}
```

---

## Email Messaging
The system sends emails for the following events:

1. **Account Verification**
   - Sent when a new user is registered
   - Contains a verification link with a time-limited token
   - Token expires after 24 hours

2. **Password Reset** (if implemented)
   - Sent when a user requests a password reset
   - Contains a secure link to reset the password

3. **Report Notifications** (if implemented)
   - Sent to relevant parties when reports are created or updated

---

## Security

### Authentication
- JWT (JSON Web Tokens) are used for authentication
- Tokens expire after a set period (default: 1 hour)
- Refresh tokens can be used to obtain new access tokens

### Authorization
- Role-based access control (RBAC) is implemented
- Available roles:
  - `ROLE_ADMIN`: Full access to all endpoints
  - `ROLE_INSPECTOR`: Can create and view reports
  - `ROLE_TEACHER`: Limited access (if implemented)

### CORS
- CORS is enabled for all origins (`*`)
- In production, restrict this to trusted domains

### Headers
- `Authorization: Bearer <token>` is required for protected endpoints
- `Content-Type: application/json` is required for endpoints with request bodies

---

## Error Handling

### Common Error Responses

**400 Bad Request**
```json
{
  "message": "Validation failed",
  "errors": [
    "Email is required",
    "Password must be at least 8 characters"
  ]
}
```

**401 Unauthorized**
```json
{
  "message": "Unauthorized: Authentication token is missing or invalid"
}
```

**403 Forbidden**
```json
{
  "message": "Access Denied: Insufficient permissions"
}
```

**404 Not Found**
```json
{
  "message": "Resource not found"
}
```
**409 Conflict (commonly used for "already exists" errors)**
```json
{
    "message": "Resource already exists"
}
```
**500 Internal Server Error**
```json
{
  "message": "An unexpected error occurred"
}
```

### Validation Errors
- Request body validation errors include detailed messages about what went wrong
- Field-level validation is performed for all input data

### Logging
- All errors are logged on the server
- Sensitive information is redacted from logs
