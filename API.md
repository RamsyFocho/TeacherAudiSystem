# Teacher Report System API Documentation

## Table of Contents
1. [Authentication](#authentication)
   - [Login](#login)
   - [Register](#register)
   - [Verify Email](#verify-email)
   - [Resend Verification Email](#resend-verification-email)
   - [Refresh Token](#refresh-token)
   - [Get Current User](#get-current-user)
   - [Update Profile](#update-profile)
   - [Forgot Password](#forgot-password)
   - [Reset Password](#reset-password)

2. [Reports](#reports)
   - [Create Report](#create-report)
   - [Get All Reports](#get-all-reports)
   - [Get Report by ID](#get-report-by-id)
   - [Update Report Sanction](#update-report-sanction)
   - [Delete Report](#delete-report)
   - [Search Reports](#search-reports)

3. [Teachers](#teachers)
    - [Get All Teachers](#get-all-teachers)
    - [Upload Teachers](#upload-teachers)
    - [Update Teacher](#update-teacher)

4. [Establishments](#establishments)
   - [Get All Establishments](#get-all-establishments)
   - [Create Establishment](#create-establishment)
   - [Create Multiple Establishments](#create-multiple-establishments)
   - [Update Establishment](#update-establishment)

5. [Analytics](#analytics)
    - [Get Overview](#get-overview)
    - [Get Reports by Establishment](#get-reports-by-establishment)
    - [Get Reports by Teacher](#get-reports-by-teacher)
    - [Get Attendance Summary](#get-attendance-summary)

6. [Dashboard](#dashboard)
    - [Get Latest Reports](#get-latest-reports)
    - [Get Teacher Performance](#get-teacher-performance)
    - [Get Establishment Performance](#get-establishment-performance)
    - [Get Reports with Sanctions](#get-reports-with-sanctions)

7. [Email Messaging](#email-messaging)
8. [Security](#security)
9. [Error Handling](#error-handling)
10. [User Roles & Permissions](#user-roles--permissions)

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
Verify a user's email address using the token sent to their email. This endpoint is a redirect GET request from the user's email client.

**Endpoint:** `GET /api/auth/verify`

**Query Parameters:**
- `token`: The verification token.
- `redirect` (optional): URL to redirect to after verification.

**Response:**
Redirects the user to the frontend with a success or error message in the query parameters.

### Resend Verification Email
Resends the verification email to a user.

**Endpoint:** `POST /api/auth/resend-verification`

**Query Parameters:**
- `email`: The user's email address.

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

### Get Current User
Get the details of the currently authenticated user.

**Endpoint:** `GET /api/auth/me`

**Headers:**
- `Authorization: Bearer <token>`

**Response:**
```json
{
    "username": "currentuser",
    "email": "currentuser@example.com",
    "enabled": true,
    "roles": [
        "ROLE_INSPECTOR"
    ]
}
```

### Update Profile
Update the profile of the currently authenticated user.

**Endpoint:** `PUT /api/auth/update/profile`

**Headers:**
- `Authorization: Bearer <token>`

**Request Body:**
```json
{
    "username": "newusername",
    "email": "newemail@example.com",
    "phoneNumber": "+1234567890",
    "address": "123 New St",
    "password": "newpassword123"
}
```

**Response:**
```json
{
    "username": "newusername",
    "email": "newemail@example.com",
    "phoneNumber": "+1234567890",
    "address": "123 New St",
    "roles": [
        "ROLE_INSPECTOR"
    ]
}
```

### Forgot Password
Initiate the password reset process for a user.

**Endpoint:** `POST /api/auth/forgot-password`

**Query Parameters:**
- `email`: The email address of the user who forgot their password.

**Response:**
```json
{
    "message": "A password reset link has been sent to your email."
}
```

### Reset Password
Set a new password using a valid reset token.

**Endpoint:** `POST /api/auth/reset-password`

**Request Body:**
```json
{
    "token": "the-reset-token-from-the-email-link",
    "newPassword": "a-strong-new-password"
}
```

**Response:**
```json
{
    "message": "Password has been reset successfully."
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
```

### Get All Reports
Get all reports.

**Endpoint:** `GET /api/reports`

**Headers:**
- `Authorization: Bearer <token>`

**Response:**
```json
[
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

### Update Report Sanction
Update an existing report's sanction and log the change (Admin only).

**Endpoint:** `PUT /api/reports/sanction/{id}`

**Headers:**
- `Authorization: Bearer <admin_token>`

**Request Body:**
```json
{
  "sanctionType": "WARNING",
  "reason": "Repeated tardiness."
}
```

**Response:**
The full updated report object.

### Soft-Delete Report
Marks a report as deleted, requiring a reason. The report is not permanently removed from the database (Admin only).

**Endpoint:** `PUT /api/reports/{id}/delete`

**Headers:**
- `Authorization: Bearer <admin_token>`

**Request Body:**
```json
{
  "reason": "Report was filed in error."
}
```

**Response:**
```json
{
    "message": "Report has been deleted successfully."
}
```

### Get Deleted Reports
Retrieves a list of all reports that have been soft-deleted (Admin only).

**Endpoint:** `GET /api/reports/deleted`

**Headers:**
- `Authorization: Bearer <admin_token>`

**Response:**
A list of soft-deleted report objects.

### Delete Report
Permanently delete a report (Admin only).

**Endpoint:** `DELETE /api/reports/{id}`

**Headers:**
- `Authorization: Bearer <admin_token>`

**Response:**
- `204 No Content` on success

### Search Reports
Search reports by various criteria.

**Endpoints:**
- `GET /api/reports/search/teacher?teacherName=John` - Search by teacher name
- `GET /api/reports/search/establishment?establishmentName=High` - Search by establishment name
- `GET /api/reports/search/class?className=10A` - Search by class name
- `GET /api/reports/search/course?courseTitle=Math` - Search by course title
- `GET /api/reports/date-range?startDate=2025-01-01T00:00:00Z&endDate=2025-12-31T23:59:59Z` - Search by date range
- `GET /api/reports/search/description?keyword=engaging` - Search in descriptions
- `GET /api/reports/search/year/{year}` - Search by year
- `GET /api/reports/sanction/{sanctionType}` - Find by sanction type
- `GET /api/reports/date-issued?dateIssued=2025-01-01T00:00:00Z` - Find by date issued
- `GET /api/reports/search/date?date=2025-01-01` - Find by specific date

### Sanction Auditing
Retrieve sanction audit logs.

**Endpoints:**
- `GET /api/reports/sanctions/teacher/{teacherId}` - Get all sanctions for a specific teacher (Admin/Director only).
- `GET /api/reports/sanctions/type/{sanctionType}` - Get all sanctions of a specific type (Admin/Director only).

---

## Teachers

### Get All Teachers
Get a list of all teachers.

**Endpoint:** `GET /api/teachers`

**Headers:**
- `Authorization: Bearer <token>`

**Response:**
```json
[
    {
        "id": 1,
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com"
    }
]
```

### Upload Teachers
Upload teachers from an Excel file (Admin or Director only).

**Endpoint:** `POST /api/teachers/upload`

**Headers:**
- `Authorization: Bearer <token>`
- `Content-Type: multipart/form-data`

**Request Body:**
- `file`: Excel file containing teacher data

**Response:**
```json
{
  "message": "Teachers uploaded successfully."
}
```

### Update Teacher
Update an existing teacher (Admin or Director only).

**Endpoint:** `PUT /api/teachers/{id}`

**Headers:**
- `Authorization: Bearer <token>`

**Request Body:**
```json
{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe.new@example.com"
}
```

**Response:**
```json
{
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe.new@example.com"
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
Create a new establishment (Admin or Director only).

**Endpoint:** `POST /api/establishments`

**Headers:**
- `Authorization: Bearer <token>`
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
Create multiple establishments at once (Admin or Director only).

**Endpoint:** `POST /api/establishments/list`

**Headers:**
- `Authorization: Bearer <token>`
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

### Update Establishment
Update an existing establishment (Admin or Director only).

**Endpoint:** `PUT /api/establishments/{id}`

**Headers:**
- `Authorization: Bearer <token>`

**Request Body:**
```json
{
    "name": "Updated School Name"
}
```

**Response:**
```json
{
    "id": 1,
    "name": "Updated School Name"
}
```

---

## Analytics

### Get Overview
Get an overview of the system data.

**Endpoint:** `GET /api/analytics/overview`

**Headers:**
- `Authorization: Bearer <token>`

**Response:**
```json
{
    "totalUsers": 10,
    "totalTeachers": 50,
    "totalEstablishments": 5,
    "totalReports": 100
}
```

### Get Reports by Establishment
Get the number of reports per establishment.

**Endpoint:** `GET /api/analytics/reports-by-establishment`

**Headers:**
- `Authorization: Bearer <token>`

**Response:**
```json
[
    {
        "establishment": "Example High School",
        "reportCount": 50
    }
]
```

### Get Reports by Teacher
Get the number of reports per teacher.

**Endpoint:** `GET /api/analytics/reports-by-teacher`

**Headers:**
- `Authorization: Bearer <token>`

**Response:**
```json
[
    {
        "teacher": "John Doe",
        "reportCount": 10
    }
]
```

### Get Attendance Summary
Get a summary of student attendance.

**Endpoint:** `GET /api/analytics/attendance-summary`

**Headers:**
- `Authorization: Bearer <token>`

**Response:**
```json
{
    "totalStudents": 1000,
    "totalPresent": 950
}
```

---

## Dashboard

### Get Latest Reports
Get a list of the most recent reports.

**Endpoint:** `GET /api/dashboard/latest-reports`

**Headers:**
- `Authorization: Bearer <token>`

**Response:**
A list of report objects.

### Get Teacher Performance
Get performance metrics for a specific teacher.

**Endpoint:** `GET /api/dashboard/teacher-performance/{teacherId}`

**Headers:**
- `Authorization: Bearer <token>`

**Response:**
```json
{
    "totalClasses": 20,
    "totalStudents": 500,
    "totalPresent": 480
}
```

### Get Establishment Performance
Get performance metrics for a specific establishment.

**Endpoint:** `GET /api/dashboard/establishment-performance/{establishmentId}`

**Headers:**
- `Authorization: Bearer <token>`

**Response:**
```json
{
    "reportCount": 50,
    "totalStudents": 1000,
    "totalPresent": 950
}
```

### Get Reports with Sanctions
Get a list of all reports that have sanctions.

**Endpoint:** `GET /api/dashboard/sanctions`

**Headers:**
- `Authorization: Bearer <token>`

**Response:**
A list of report objects with sanctions.

---

## Audit Trail

### Get Audit Logs
Get a paginated list of all audit logs (Admin only).

**Endpoint:** `GET /api/audit`

**Headers:**
- `Authorization: Bearer <admin_token>`

**Query Parameters:**
- `page`: The page number to retrieve.
- `size`: The number of items per page.
- `sort`: Sorting criteria (e.g., `timestamp,desc`).

**Response:**
A paginated list of audit log objects.

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