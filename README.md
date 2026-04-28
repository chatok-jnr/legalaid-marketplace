# Legal Marketplace

A Spring Boot REST API for a legal aid marketplace.

## Overview

This service manages:

- lawyer gigs
- gig media assets
- gig reviews
- Cloudinary file uploads and deletes
- JWT-based authentication and role-based authorization

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Spring Security
- PostgreSQL
- Cloudinary
- JWT

## Authentication

All API routes under `/api/**` require a valid JWT **except** `/api/validate/**`, which is explicitly allowed in `SecurityConfig`.

### Authorization header

```http
Authorization: Bearer <jwt-token>
```

### JWT expectations

The JWT filter reads:

- the subject as the authenticated user email
- roles from either `roles` or legacy `role` claims

Role names are converted to Spring Security authorities like `ROLE_LAWYER`, `ROLE_CLIENT`, and `ROLE_ADMIN`.

## RBAC Summary

| Role | Meaning |
|---|---|
| `LAWYER` | Can manage own gigs, media, reviews, and file uploads |
| `CLIENT` | Can read gig media and create/update reviews |
| `ADMIN` | Can read gig media and delete gig media |

## API Endpoints

### 1) Gig management

| Method | Endpoint | Secured | Roles Allowed |
|---|---|---:|---|
| `POST` | `/api/gigs/me` | Yes | `LAWYER` |
| `PATCH` | `/api/gigs/me/{id}` | Yes | `LAWYER` |
| `GET` | `/api/gigs/me` | Yes | `LAWYER` |
| `GET` | `/api/gigs/public` | Yes | `LAWYER`, `CLIENT`, `ADMIN` |
| `GET` | `/api/gigs/public/{gigId}` | Yes | `LAWYER`, `CLIENT`, `ADMIN` |
| `DELETE` | `/api/gigs/me/{id}` | Yes | `LAWYER` |

#### `POST /api/gigs/me`
Create a gig for the authenticated lawyer.

**Request body**

```json
{
  "title": "Family law consultation",
  "minPrice": 100,
  "aboutThisGig": "Advice on divorce, custody, and related matters.",
  "public": true
}
```

> Note: the DTO field is `isPublic`; in JSON it is typically serialized as `public` by Jackson.

**Response body** (`201 Created`)

```json
{
  "id": "d8f9f8a8-8f4a-4b6a-9d03-8cf6d6f2f5a4",
  "lawyerId": "3c7c1a34-2a8a-4a67-8bd1-0aa9f0ef66f1",
  "title": "Family law consultation",
  "minPrice": 100,
  "aboutThisGig": "Advice on divorce, custody, and related matters.",
  "public": true,
  "createdAt": "2026-04-25T10:00:00Z",
  "updatedAt": "2026-04-25T10:00:00Z"
}
```

#### `PATCH /api/gigs/me/{id}`
Update an existing gig owned by the authenticated lawyer.

**Request body**

```json
{
  "title": "Updated family law consultation",
  "minPrice": 150,
  "aboutThisGig": "Updated description.",
  "public": false
}
```

**Response body** (`200 OK`)

```json
{
  "id": "d8f9f8a8-8f4a-4b6a-9d03-8cf6d6f2f5a4",
  "lawyerId": "3c7c1a34-2a8a-4a67-8bd1-0aa9f0ef66f1",
  "title": "Updated family law consultation",
  "minPrice": 150,
  "aboutThisGig": "Updated description.",
  "public": false,
  "createdAt": "2026-04-25T10:00:00Z",
  "updatedAt": "2026-04-25T10:10:00Z"
}
```

#### `GET /api/gigs/me`
Get all gigs owned by the authenticated lawyer.

**Request body**: none

**Response body** (`200 OK`)

```json
[
  {
    "id": "d8f9f8a8-8f4a-4b6a-9d03-8cf6d6f2f5a4",
    "lawyerId": "3c7c1a34-2a8a-4a67-8bd1-0aa9f0ef66f1",
    "title": "Family law consultation",
    "minPrice": 100,
    "aboutThisGig": "Advice on divorce, custody, and related matters.",
    "public": true,
    "createdAt": "2026-04-25T10:00:00Z",
    "updatedAt": "2026-04-25T10:00:00Z"
  }
]
```

#### `GET /api/gigs/public`
Get all gigs marked as public.

**Query parameters**

| Name | Type | Required | Default | Description |
|---|---|---:|---|---|
| `page` | integer | No | `0` | Page number, starting from 0 |
| `size` | integer | No | `30` | Number of items per page |
| `sort` | string | No | `id,desc` | Sort field and direction |

> Defaults come from `@PageableDefault(size = 30, sort = "id", direction = Sort.Direction.DESC)` in `GigController`.

**Request body**: none

**Response body** (`200 OK`)

```json
{
  "content": [
    {
      "lawyerName": "John Doe",
      "lawyerProfilePicUrl": "https://example.com/profile.jpg",
      "id": "d8f9f8a8-8f4a-4b6a-9d03-8cf6d6f2f5a4",
      "title": "Family law consultation",
      "lawyerId": "3c7c1a34-2a8a-4a67-8bd1-0aa9f0ef66f1",
      "minPrice": 100,
      "allMedia": [
        {
          "url": "https://example.com/media-1.jpg",
          "serial_no": 1
        },
        {
          "url": "https://example.com/media-2.jpg",
          "serial_no": 2
        }
      ]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 30,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "last": true,
  "totalPages": 1,
  "totalElements": 1,
  "size": 30,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
```

#### `GET /api/gigs/public/{gigId}`
Get one public gig by its gig ID.

**Path params**

- `gigId` (UUID): gig identifier

**Request body**: none

**Response body** (`200 OK`)

```json
{
  "id": "d8f9f8a8-8f4a-4b6a-9d03-8cf6d6f2f5a4",
  "lawyerId": "3c7c1a34-2a8a-4a67-8bd1-0aa9f0ef66f1",
  "title": "Family law consultation",
  "minPrice": 100,
  "aboutThisGig": "Advice on divorce, custody, and related matters.",
  "updatedAt": "2026-04-25T10:00:00Z"
}
```

#### `DELETE /api/gigs/me/{id}`
Delete one of the authenticated lawyer’s gigs.

**Request body**: none

**Response body** (`204 No Content`)

No response body.

---

### 2) Gig media

| Method | Endpoint | Secured | Roles Allowed |
|---|---|---:|---|
| `POST` | `/api/gigs/media/` | Yes | `LAWYER` |
| `PATCH` | `/api/gigs/media/{gigId}/{serialNo}` | Yes | `LAWYER` |
| `GET` | `/api/gigs/media/{gigId}/{serialNo}` | Yes | `LAWYER`, `CLIENT`, `ADMIN` |
| `GET` | `/api/gigs/media/{gigId}` | Yes | `LAWYER`, `CLIENT`, `ADMIN` |
| `DELETE` | `/api/gigs/media/{gigId}/{serialNo}` | Yes | `LAWYER`, `ADMIN` |
| `DELETE` | `/api/gigs/media/{gigId}` | Yes | `LAWYER`, `ADMIN` |

#### `POST /api/gigs/media/`
Create a media record for a gig.

**Request body**

```json
{
  "gigId": "d8f9f8a8-8f4a-4b6a-9d03-8cf6d6f2f5a4",
  "serialNo": 1,
  "url": "https://res.cloudinary.com/demo/image/upload/sample.jpg",
  "publicId": "legalAid/lawyer-gigs-media/sample",
  "resourceType": "image"
}
```

**Response body** (`201 Created`)

```json
{
  "gigId": "d8f9f8a8-8f4a-4b6a-9d03-8cf6d6f2f5a4",
  "serialNo": 1,
  "url": "https://res.cloudinary.com/demo/image/upload/sample.jpg",
  "publicId": "legalAid/lawyer-gigs-media/sample",
  "resourceType": "image",
  "createdAt": "2026-04-25T10:00:00Z",
  "updatedAt": "2026-04-25T10:00:00Z"
}
```

#### `PATCH /api/gigs/media/{gigId}/{serialNo}`
Update the serial number of a gig media record.

**Request body**

```json
{
  "newSerialNo": 2
}
```

**Response body** (`200 OK`)

```json
{
  "gigId": "d8f9f8a8-8f4a-4b6a-9d03-8cf6d6f2f5a4",
  "serialNo": 2,
  "url": "https://res.cloudinary.com/demo/image/upload/sample.jpg",
  "publicId": "legalAid/lawyer-gigs-media/sample",
  "resourceType": "image",
  "createdAt": "2026-04-25T10:00:00Z",
  "updatedAt": "2026-04-25T10:10:00Z"
}
```

#### `GET /api/gigs/media/{gigId}/{serialNo}`
Get one media item by gig ID and serial number.

**Request body**: none

**Response body** (`200 OK`)

```json
{
  "gigId": "d8f9f8a8-8f4a-4b6a-9d03-8cf6d6f2f5a4",
  "serialNo": 1,
  "url": "https://res.cloudinary.com/demo/image/upload/sample.jpg",
  "publicId": "legalAid/lawyer-gigs-media/sample",
  "resourceType": "image",
  "createdAt": "2026-04-25T10:00:00Z",
  "updatedAt": "2026-04-25T10:00:00Z"
}
```

#### `GET /api/gigs/media/{gigId}`
Get all media items for a gig.

**Request body**: none

**Response body** (`200 OK`)

```json
[
  {
    "gigId": "d8f9f8a8-8f4a-4b6a-9d03-8cf6d6f2f5a4",
    "serialNo": 1,
    "url": "https://res.cloudinary.com/demo/image/upload/sample.jpg",
    "publicId": "legalAid/lawyer-gigs-media/sample",
    "resourceType": "image",
    "createdAt": "2026-04-25T10:00:00Z",
    "updatedAt": "2026-04-25T10:00:00Z"
  }
]
```

#### `DELETE /api/gigs/media/{gigId}/{serialNo}`
Delete one media item.

**Request body**: none

**Response body** (`204 No Content`)

No response body.

#### `DELETE /api/gigs/media/{gigId}`
Delete all media for a gig.

**Request body**: none

**Response body** (`204 No Content`)

No response body.

---

### 3) Gig reviews

| Method | Endpoint | Secured | Roles Allowed |
|---|---|---:|---|
| `POST` | `/api/gigs/reviews/` | Yes | `LAWYER`, `CLIENT` |
| `PATCH` | `/api/gigs/reviews/` | Yes | `LAWYER`, `CLIENT` |

#### `POST /api/gigs/reviews/`
Create a review for a gig.

**Request body**

```json
{
  "gigId": "d8f9f8a8-8f4a-4b6a-9d03-8cf6d6f2f5a4",
  "rating": 5,
  "comment": "Very helpful and professional."
}
```

**Response body** (`201 Created`)

```json
{
  "gigId": "d8f9f8a8-8f4a-4b6a-9d03-8cf6d6f2f5a4",
  "userId": "b2e2caa2-3f50-4a11-b72f-5e1d6f93e3a1",
  "rating": 5,
  "comment": "Very helpful and professional.",
  "createdAt": "2026-04-25T10:00:00Z",
  "updatedAt": "2026-04-25T10:00:00Z"
}
```

#### `PATCH /api/gigs/reviews/`
Update a review created by the authenticated user.

**Request body**

```json
{
  "gigId": "d8f9f8a8-8f4a-4b6a-9d03-8cf6d6f2f5a4",
  "rating": 4,
  "comment": "Still good, but could improve response time."
}
```

**Response body** (`200 OK`)

```json
{
  "gigId": "d8f9f8a8-8f4a-4b6a-9d03-8cf6d6f2f5a4",
  "userId": "b2e2caa2-3f50-4a11-b72f-5e1d6f93e3a1",
  "rating": 4,
  "comment": "Still good, but could improve response time.",
  "createdAt": "2026-04-25T10:00:00Z",
  "updatedAt": "2026-04-25T10:10:00Z"
}
```

---

### 4) File uploads

| Method | Endpoint | Secured | Roles Allowed |
|---|---|---:|---|
| `POST` | `/api/files/upload` | Yes | `LAWYER` |
| `DELETE` | `/api/files/delete` | Yes | `LAWYER` |

#### `POST /api/files/upload`
Upload a file to Cloudinary.

**Request body**

`multipart/form-data`

| Field | Type | Required |
|---|---|---:|
| `file` | file | Yes |

Example form data:

```text
file: <binary file>
```

**Response body** (`200 OK`)

```json
{
  "url": "https://res.cloudinary.com/demo/image/upload/v1234567890/sample.jpg",
  "publicId": "legalAid/lawyer-gigs-media/sample"
}
```

#### `DELETE /api/files/delete`
Delete a file from Cloudinary.

**Request body**: none

**Query parameters**

| Name | Type | Required | Default |
|---|---|---:|---|
| `publicId` | string | Yes | - |
| `resourceType` | string | No | `image` |

Example:

```text
/api/files/delete?publicId=legalAid/lawyer-gigs-media/sample&resourceType=image
```

**Response body** (`200 OK`)

```json
{
  "message": "File deleted successfully",
  "publicId": "legalAid/lawyer-gigs-media/sample"
}
```

If deletion fails, the API returns an error object like:

```json
{
  "error": "Failed to delete file: not found"
}
```

## Database Schema

The application uses PostgreSQL and `spring.jpa.hibernate.ddl-auto=update`.

### `users`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | UUID | primary key | User identifier |
| `full_name` | varchar | not null | Full name |
| `username` | varchar | not null, unique | Login/display username |
| `email` | varchar | not null, unique | Used as JWT subject / authenticated principal |

### `gigs`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | UUID | primary key | Generated UUID |
| `lawyer_id` | UUID | not null | Owner lawyer ID |
| `title` | varchar(100) | not null | Gig title |
| `min_price` | integer | not null | Minimum price |
| `about_this_gig` | text | nullable | Long description |
| `is_public` | boolean | nullable/default true | Visibility flag |
| `created_at` | timestamp with time zone | not null | Auto-created |
| `updated_at` | timestamp with time zone | not null | Auto-updated |

### `gig_media`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `gigId` | UUID | composite primary key, FK to `gigs.id` | Gig reference |
| `serialNo` | integer | composite primary key, min 1 | Media order/key |
| `url` | varchar | not null | Cloudinary secure URL |
| `public_id` | varchar | not null | Cloudinary public ID |
| `resource_type` | varchar | not null | Usually `image`, `video`, or `raw` |
| `createdAt` | timestamp with time zone | entity-managed | Creation time |
| `updatedAt` | timestamp with time zone | entity-managed | Update time |

### `gig_reviews`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `gigId` | UUID | composite primary key, FK to `gigs.id` | Reviewed gig |
| `userId` | UUID | composite primary key | Reviewer user |
| `rating` | integer | not null, 1-5 | Review score |
| `comment` | text | nullable | Optional feedback |
| `created_at` | timestamp with time zone | not null | Auto-created |
| `updated_at` | timestamp with time zone | not null | Auto-updated |

### `UserRole` enum

The application role enum is:

- `LAWYER`
- `CLIENT`
- `ADMIN`

It is used for authorization, not as a separate database table in this codebase.

## Configuration

### CORS

Frontend origin allowed by `CorsConfig`:

- `http://localhost:3000`

Allowed methods:

- `GET`
- `POST`
- `PUT`
- `DELETE`
- `PATCH`

### Environment variables

The app expects these secrets/config values:

- `JWT_SECRET`
- `JWT_EXPIRATION_MINUTES` (optional, defaults to `15`)
- `CLOUDINARY_CLOUD_NAME`
- `CLOUDINARY_API_KEY`
- `CLOUDINARY_API_SECRET`
- `CLOUDINARY_UPLOAD_FOLDER`

## Run the project

```bash
./mvnw spring-boot:run
```

Or build first:

```bash
./mvnw clean test
./mvnw clean package
```

## Notes

- Every secured endpoint requires a valid `Authorization: Bearer ...` token.
- The codebase currently exposes `/api/validate/**` as public in security config, but no controller for that route is present in this repository.
- File upload/delete operations talk to Cloudinary and return compact JSON maps rather than typed DTOs.
