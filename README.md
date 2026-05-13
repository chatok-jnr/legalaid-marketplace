# Legal Marketplace

A Spring Boot REST API for a legal aid marketplace.

## Overview

This service manages:

- lawyer gigs
- gig media assets
- gig reviews
- contract creation, delivery, payment, and dispute workflows
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

## Docker

The repo includes a root-level [Dockerfile](./Dockerfile) that builds the packaged Spring Boot jar into a Java 21 Alpine image.

```bash
./mvnw clean package -DskipTests
docker build -t legal-marketplace:local .
docker run --rm -p 8080:8080 --env-file .env legal-marketplace:local
```

The container exposes port `8080`.

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
Get one public gig by its gig ID with nested gig basic info, media list, and detailed lawyer profile information.

**Path params**

- `gigId` (UUID): gig identifier

**Request body**: none

**Response body** (`200 OK`)

```json
{
  "gigBasicInfo": {
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
  },
  "barNumber": "NY123456",
  "bio": "Experienced family law attorney with 10+ years of practice.",
  "specializations": ["Family Law", "Divorce", "Custody"],
  "yearsExperience": 10,
  "aboutThisGig": "Advice on divorce, custody, and related matters.",
  "memberSince": "2020-06-15T08:30:00Z"
}
```

**Response fields**

Top-level fields:

| Field | Type | Description |
|---|---|---|
| `gigBasicInfo` | object | Public gig summary with lawyer name and profile picture |
| `barNumber` | string | Lawyer's bar registration number |
| `bio` | string | Lawyer's professional biography |
| `specializations` | array[string] | List of legal specializations |
| `yearsExperience` | integer | Years of professional experience |
| `aboutThisGig` | string | Gig-specific description |
| `memberSince` | ISO 8601 timestamp | When the lawyer joined the platform |

Nested `gigBasicInfo` fields:

| Field | Type | Description |
|---|---|---|
| `lawyerName` | string | Lawyer's full name |
| `lawyerProfilePicUrl` | string | Lawyer's profile picture URL |
| `id` | UUID | Gig identifier |
| `title` | string | Gig title |
| `lawyerId` | UUID | Lawyer identifier |
| `minPrice` | integer | Minimum price for the gig |
| `allMedia` | array[object] | Gig media items, each with `url` and `serial_no` |

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

---

### 5) Contracts, deliveries, payments, and disputes

| Method | Endpoint | Secured | Roles Allowed |
|---|---|---:|---|
| `POST` | `/api/contract` | Yes | `CLIENT` |
| `GET` | `/api/contract/me/{role}` | Yes | `CLIENT`, `LAWYER` |
| `GET` | `/api/contract/{contractId}` | Yes | `CLIENT`, `LAWYER` |
| `PATCH` | `/api/contract/{contractId}/cancel` | Yes | `CLIENT`, `LAWYER` |
| `POST` | `/api/contract-deliveries` | Yes | `LAWYER` |
| `GET` | `/api/contract-deliveries/contract/{contractId}` | Yes | `CLIENT`, `LAWYER` |
| `GET` | `/api/contract-deliveries/{deliveryId}` | Yes | `CLIENT`, `LAWYER` |
| `POST` | `/api/deliveries/{deliveryId}/files` | Yes | `LAWYER` |
| `POST` | `/api/deliveries/{deliveryId}/files/upload` | Yes | `LAWYER` |
| `DELETE` | `/api/deliveries/{deliveryId}/files/{fileId}` | Yes | `LAWYER` |
| `POST` | `/api/payments` | Yes | `CLIENT` |
| `POST` | `/api/disputes` | Yes | `CLIENT`, `LAWYER` |

#### `POST /api/contract`
Create a new contract for the authenticated client.

**Request body**

```json
{
  "gigId": "d8f9f8a8-8f4a-4b6a-9d03-8cf6d6f2f5a4",
  "requirements": "Need help with divorce filing and custody planning.",
  "deliveryDeadline": "2026-05-10T15:00:00Z"
}
```

**Response body** (`201 Created`)

```json
{
  "id": "f2e9a2c5-5f1d-4ed4-89d9-31a8d5d6bb20",
  "clientId": "b2e2caa2-3f50-4a11-b72f-5e1d6f93e3a1",
  "lawyerId": "3c7c1a34-2a8a-4a67-8bd1-0aa9f0ef66f1",
  "gigId": "d8f9f8a8-8f4a-4b6a-9d03-8cf6d6f2f5a4",
  "priceAtHire": 100,
  "requirements": "Need help with divorce filing and custody planning.",
  "revisionsLeft": 3,
  "status": "PENDING",
  "deliveryDeadline": "2026-05-10T15:00:00Z",
  "createdAt": "2026-04-25T10:00:00Z"
}
```

#### `GET /api/contract/me/{role}`
Get the authenticated user's contracts by role.

**Path params**

- `role` (string): must be `CLIENT` or `LAWYER`

**Request body**: none

**Response body** (`200 OK`)

```json
[
  {
    "gigTitle": "Family law consultation",
    "clientName": "Jane Client",
    "lawyerName": "John Lawyer",
    "priceAtHire": 100,
    "status": "ACTIVE",
    "createdAt": "2026-04-25T10:00:00Z"
  }
]
```

#### `GET /api/contract/{contractId}`
Get the full contract details for the authenticated user.

**Path params**

- `contractId` (UUID): contract identifier

**Request body**: none

**Response body** (`200 OK`)

```json
{
  "clientId": "b2e2caa2-3f50-4a11-b72f-5e1d6f93e3a1",
  "lawyerId": "3c7c1a34-2a8a-4a67-8bd1-0aa9f0ef66f1",
  "gigId": "d8f9f8a8-8f4a-4b6a-9d03-8cf6d6f2f5a4",
  "clientName": "Jane Client",
  "lawyerName": "John Lawyer",
  "gigTitle": "Family law consultation",
  "priceAtHire": 100,
  "requirements": "Need help with divorce filing and custody planning.",
  "revisionsLeft": 3,
  "status": "ACTIVE",
  "cancelledAt": null,
  "cancellationReason": null,
  "cancelledBy": null,
  "deliveryDeadline": "2026-05-10T15:00:00Z",
  "createdAt": "2026-04-25T10:00:00Z",
  "updatedAt": "2026-04-25T10:05:00Z",
  "paymentId": "4c0d5ef7-2c55-4c10-87ad-3d15dd5a2a11",
  "platformFeeAmount": 10,
  "lawyerPayoutAmount": 90,
  "paymentReference": "PAY-001",
  "paymentStatus": "PENDING",
  "paidAt": null,
  "escrowReleasedAt": null,
  "disputeId": null,
  "disputeStatus": null,
  "disputeReason": null,
  "disputeOpenedBy": null,
  "disputeAdminId": null,
  "disputeOpenedAt": null,
  "disputeResolvedAt": null,
  "disputeResolution": null
}
```

#### `PATCH /api/contract/{contractId}/cancel`
Cancel an existing contract.

**Request body**

```json
{
  "cancelledReason": "Timeline no longer works for me."
}
```

**Response body** (`200 OK`)

```json
{
  "clientId": "b2e2caa2-3f50-4a11-b72f-5e1d6f93e3a1",
  "lawyerId": "3c7c1a34-2a8a-4a67-8bd1-0aa9f0ef66f1",
  "gigId": "d8f9f8a8-8f4a-4b6a-9d03-8cf6d6f2f5a4",
  "clientName": "Jane Client",
  "lawyerName": "John Lawyer",
  "gigTitle": "Family law consultation",
  "priceAtHire": 100,
  "requirements": "Need help with divorce filing and custody planning.",
  "revisionsLeft": 3,
  "status": "CANCELLED",
  "cancelledAt": "2026-04-25T11:00:00Z",
  "cancellationReason": "Timeline no longer works for me.",
  "cancelledBy": "b2e2caa2-3f50-4a11-b72f-5e1d6f93e3a1",
  "deliveryDeadline": "2026-05-10T15:00:00Z",
  "createdAt": "2026-04-25T10:00:00Z",
  "updatedAt": "2026-04-25T11:00:00Z",
  "paymentId": "4c0d5ef7-2c55-4c10-87ad-3d15dd5a2a11",
  "platformFeeAmount": 10,
  "lawyerPayoutAmount": 90,
  "paymentReference": "PAY-001",
  "paymentStatus": "REFUNDED",
  "paidAt": "2026-04-25T10:05:00Z",
  "escrowReleasedAt": null,
  "disputeId": null,
  "disputeStatus": null,
  "disputeReason": null,
  "disputeOpenedBy": null,
  "disputeAdminId": null,
  "disputeOpenedAt": null,
  "disputeResolvedAt": null,
  "disputeResolution": null
}
```

#### `POST /api/contract-deliveries`
Create a new delivery for a contract.

**Request body**

```json
{
  "contractId": "f2e9a2c5-5f1d-4ed4-89d9-31a8d5d6bb20",
  "deliveryNote": "Initial draft is ready for review."
}
```

**Response body** (`201 Created`)

```json
{
  "id": "8c6df1c6-7258-4d8c-99b8-3b233ecfbd5d",
  "contractId": "f2e9a2c5-5f1d-4ed4-89d9-31a8d5d6bb20",
  "deliveryNumber": 1,
  "deliveredBy": "3c7c1a34-2a8a-4a67-8bd1-0aa9f0ef66f1",
  "deliveryNote": "Initial draft is ready for review.",
  "revisionRequestedAt": null,
  "revisionNote": null,
  "deliveredAt": "2026-04-25T12:00:00Z",
  "completedAt": null
}
```

#### `GET /api/contract-deliveries/contract/{contractId}`
Get all deliveries for a contract.

**Request body**: none

**Response body** (`200 OK`)

```json
[
  {
    "id": "8c6df1c6-7258-4d8c-99b8-3b233ecfbd5d",
    "contractId": "f2e9a2c5-5f1d-4ed4-89d9-31a8d5d6bb20",
    "deliveryNumber": 1,
    "deliveredBy": "3c7c1a34-2a8a-4a67-8bd1-0aa9f0ef66f1",
    "deliveryNote": "Initial draft is ready for review.",
    "revisionRequestedAt": null,
    "revisionNote": null,
    "deliveredAt": "2026-04-25T12:00:00Z",
    "completedAt": null
  }
]
```

#### `GET /api/contract-deliveries/{deliveryId}`
Get the full delivery details, including attached files.

**Request body**: none

**Response body** (`200 OK`)

```json
{
  "deliveryId": "8c6df1c6-7258-4d8c-99b8-3b233ecfbd5d",
  "clientId": "b2e2caa2-3f50-4a11-b72f-5e1d6f93e3a1",
  "lawyerId": "3c7c1a34-2a8a-4a67-8bd1-0aa9f0ef66f1",
  "contractId": "f2e9a2c5-5f1d-4ed4-89d9-31a8d5d6bb20",
  "deliveryNote": "Initial draft is ready for review.",
  "deliveredAt": "2026-04-25T12:00:00Z",
  "revisionRequestedAt": null,
  "revisionNote": null,
  "completedAt": null,
  "deliveryFiles": [
    {
      "fileName": "draft.pdf",
      "url": "https://example.com/draft.pdf",
      "fileSize": 245000,
      "mimeType": "application/pdf"
    }
  ]
}
```

#### `POST /api/deliveries/{deliveryId}/files`
Register an already uploaded file for a delivery.

**Request body**

```json
{
  "fileName": "draft.pdf",
  "fileUrl": "https://example.com/draft.pdf",
  "fileSize": 245000,
  "mimeType": "application/pdf"
}
```

**Response body** (`201 Created`)

```json
{
  "id": "c1f5ecf4-3e4b-4f86-8d78-4b3adf7d2c20",
  "deliveryId": "8c6df1c6-7258-4d8c-99b8-3b233ecfbd5d",
  "fileName": "draft.pdf",
  "fileUrl": "https://example.com/draft.pdf",
  "fileSize": 245000,
  "mimeType": "application/pdf",
  "uploadedBy": "3c7c1a34-2a8a-4a67-8bd1-0aa9f0ef66f1",
  "createdAt": "2026-04-25T12:05:00Z"
}
```

#### `POST /api/deliveries/{deliveryId}/files/upload`
Upload a file to Cloudinary and register it for a delivery.

**Request body**

`multipart/form-data`

| Field | Type | Required |
|---|---|---:|
| `file` | file | Yes |

**Response body** (`201 Created`)

```json
{
  "id": "c1f5ecf4-3e4b-4f86-8d78-4b3adf7d2c20",
  "deliveryId": "8c6df1c6-7258-4d8c-99b8-3b233ecfbd5d",
  "fileName": "draft.pdf",
  "fileUrl": "https://res.cloudinary.com/demo/image/upload/v1234567890/draft.pdf",
  "fileSize": 245000,
  "mimeType": "application/pdf",
  "uploadedBy": "3c7c1a34-2a8a-4a67-8bd1-0aa9f0ef66f1",
  "createdAt": "2026-04-25T12:05:00Z"
}
```

#### `DELETE /api/deliveries/{deliveryId}/files/{fileId}`
Delete a delivery file registration. Only the authenticated lawyer who uploaded the file can delete it.

**Request body**: none

**Response body** (`204 No Content`)

_No content_

> Note: this endpoint removes the delivery-file record from the database. The current file model stores `fileUrl` only, so Cloudinary asset deletion would require storing a Cloudinary `publicId` as well.

> Note: there is no delete-all deliveries endpoint in the current `ContractDeliveryController` implementation.

---

### 6) Payments

| Method | Endpoint | Secured | Roles Allowed |
|---|---|---:|---|
| `POST` | `/api/payments` | Yes | `CLIENT` |

#### `POST /api/payments`
Create a payment record for a contract.

**Request body**

```json
{
  "contractId": "f2e9a2c5-5f1d-4ed4-89d9-31a8d5d6bb20",
  "paymentReference": "PAY-001"
}
```

**Response body** (`201 Created`)

```json
{
  "id": "4c0d5ef7-2c55-4c10-87ad-3d15dd5a2a11",
  "contractId": "f2e9a2c5-5f1d-4ed4-89d9-31a8d5d6bb20",
  "platformFeeAmount": 10,
  "platformFeePercent": 10,
  "lawyerPayoutAmount": 90,
  "paymentStatus": "PENDING",
  "paymentReference": "PAY-001"
}
```

---

### 7) Disputes

| Method | Endpoint | Secured | Roles Allowed |
|---|---|---:|---|
| `POST` | `/api/disputes` | Yes | `CLIENT`, `LAWYER` |

#### `POST /api/disputes`
Open a dispute for a contract.

**Request body**

```json
{
  "contractId": "f2e9a2c5-5f1d-4ed4-89d9-31a8d5d6bb20",
  "disputeReason": "The delivered draft does not match the requested scope."
}
```

**Response body** (`201 Created`)

```json
{
  "disputeId": "caa3f0dc-d1c6-4f6c-9e19-8e2f2b8b7c53",
  "contractId": "f2e9a2c5-5f1d-4ed4-89d9-31a8d5d6bb20",
  "disputeReason": "The delivered draft does not match the requested scope.",
  "disputeStatus": "OPEN",
  "disputeOpenedBy": "b2e2caa2-3f50-4a11-b72f-5e1d6f93e3a1"
}
```

> Note: `PATCH /api/contract-deliveries/{contractDeliveryId}/accept` exists in `ContractDeliveryController`, but it currently returns `null` and is not documented here as an active endpoint yet.

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

### `contracts`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | UUID | primary key | Contract identifier |
| `client_id` | UUID | not null | Client user ID |
| `lawyer_id` | UUID | not null | Lawyer user ID |
| `gig_id` | UUID | not null | Associated gig ID |
| `price_at_hire` | integer | not null | Agreed price |
| `requirements` | text | not null | Client's requirements |
| `revisions_left` | integer | not null | Number of revisions left |
| `status` | varchar | not null | Contract status (e.g., PENDING, ACTIVE, CANCELLED) |
| `delivery_deadline` | timestamp with time zone | not null | Delivery deadline |
| `created_at` | timestamp with time zone | not null | Auto-created |
| `updated_at` | timestamp with time zone | not null | Auto-updated |

### `contract_deliveries`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | UUID | primary key | Delivery identifier |
| `contract_id` | UUID | not null | Associated contract ID |
| `delivery_number` | integer | not null | Delivery sequence number |
| `delivered_by` | UUID | not null | Lawyer who made the delivery |
| `delivery_note` | text | nullable | Note about the delivery |
| `revision_requested_at` | timestamp with time zone | nullable | When revision was requested |
| `revision_note` | text | nullable | Note about the revision |
| `delivered_at` | timestamp with time zone | not null | When the delivery was made |
| `completed_at` | timestamp with time zone | nullable | When the delivery was completed |

### `payment_transactions`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | UUID | primary key | Payment identifier |
| `contract_id` | UUID | not null | Associated contract ID |
| `platform_fee_amount` | integer | not null | Fee amount charged by the platform |
| `platform_fee_percent` | integer | not null | Fee percentage charged by the platform |
| `lawyer_payout_amount` | integer | not null | Amount paid out to the lawyer |
| `payment_status` | varchar | not null | Status of the payment (e.g., PENDING, COMPLETED, REFUNDED) |
| `payment_reference` | varchar | not null | External payment reference ID |

### `disputes`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | UUID | primary key | Dispute identifier |
| `contract_id` | UUID | not null | Associated contract ID |
| `dispute_reason` | text | not null | Reason for the dispute |
| `dispute_status` | varchar | not null | Status of the dispute (e.g., OPEN, CLOSED) |
| `dispute_opened_by` | UUID | not null | User ID of the person who opened the dispute |
| `dispute_admin_id` | UUID | nullable | Admin user ID assigned to the dispute |
| `dispute_opened_at` | timestamp with time zone | nullable | When the dispute was opened |
| `dispute_resolved_at` | timestamp with time zone | nullable | When the dispute was resolved |
| `dispute_resolution` | text | nullable | Resolution details for the dispute |

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
