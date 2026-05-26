# API Contracts

This document summarizes the public HTTP contracts currently defined for Client Fidelity.

Base content type:

```http
Content-Type: application/json
Accept: application/json
```

## Shared Error Response

Validation and most business errors use this shape:

```json
{
  "code": "VALIDATION_FAILED",
  "message": "The request contains invalid fields.",
  "details": [
    {
      "field": "email",
      "message": "Email must be valid."
    }
  ]
}
```

`details` may be omitted for non-field errors.

## Create User

```http
POST /users
```

Creates a commerce or commerce client.

### Commerce Request

```json
{
  "type": "COMMERCE",
  "name": "Padaria Central",
  "contactName": "Maria Silva",
  "email": "maria@padariacentral.com.br",
  "phone": "+5511999999999",
  "governmentIdentifier": "12345678000195"
}
```

### Commerce Client Request

```json
{
  "type": "COMMERCE_CLIENT",
  "name": "Joao Souza",
  "email": "joao@email.com",
  "phone": "+5511988888888"
}
```

### Request Fields

| Field | Type | Required | Rules |
| --- | --- | --- | --- |
| `type` | string | Yes | `COMMERCE` or `COMMERCE_CLIENT`. |
| `name` | string | Yes | Must not be blank after trimming. |
| `contactName` | string | Commerce only | Required for commerce. Rejected for commerce client. |
| `email` | string | Yes | Must be valid. Normalized to lowercase. |
| `phone` | string | Yes | Must be valid. Normalized before persistence. |
| `governmentIdentifier` | string | Commerce only | Brazilian CNPJ. Required for commerce. Rejected for commerce client. |

### Success Response

```http
201 Created
Location: /users/{userId}
```

Commerce:

```json
{
  "id": "usr_01HX9M6VQ8Y7QK4Z3J5A2B1C0D",
  "type": "COMMERCE",
  "name": "Padaria Central",
  "contactName": "Maria Silva",
  "email": "maria@padariacentral.com.br",
  "phone": "+5511999999999",
  "governmentIdentifier": "12345678000195",
  "createdAt": "2026-05-25T23:30:00Z"
}
```

Commerce client:

```json
{
  "id": "usr_01HX9M8T5W47BJRZP9N8S3K2A1",
  "type": "COMMERCE_CLIENT",
  "name": "Joao Souza",
  "email": "joao@email.com",
  "phone": "+5511988888888",
  "createdAt": "2026-05-25T23:30:00Z"
}
```

### Error Responses

| Status | Code | When |
| --- | --- | --- |
| `400 Bad Request` | `MALFORMED_JSON` | Request body is not valid JSON or cannot be parsed. |
| `400 Bad Request` | `UNKNOWN_USER_TYPE` | `type` is missing or unsupported. |
| `409 Conflict` | `COMMERCE_ALREADY_EXISTS` | Commerce already exists for the provided CNPJ. |
| `409 Conflict` | `COMMERCE_CLIENT_ALREADY_EXISTS` | Commerce client already exists for the provided email. |
| `422 Unprocessable Entity` | `VALIDATION_FAILED` | Field validation failed. |
| `500 Internal Server Error` | `USER_CREATION_FAILED` | Unexpected creation failure. |

## Request Authentication Code

```http
POST /auth/codes
```

Requests a short-lived authentication code using email or phone.

### Email Request

```json
{
  "method": "EMAIL",
  "identifier": "customer@email.com"
}
```

### Phone Request

```json
{
  "method": "PHONE",
  "identifier": "+5511999999999"
}
```

### Request Fields

| Field | Type | Required | Rules |
| --- | --- | --- | --- |
| `method` | string | Yes | `EMAIL` or `PHONE`. |
| `identifier` | string | Yes | Email for `EMAIL`; phone for `PHONE`. Normalized before lookup. |

### Success Response

```http
202 Accepted
```

```json
{
  "challengeId": "cha_01HX9M6VQ8Y7QK4Z3J5A2B1C0D",
  "method": "EMAIL",
  "expiresAt": "2026-05-25T23:35:00Z"
}
```

For unknown users, the API also returns `202 Accepted` but does not send a code or create a challenge. This avoids account enumeration.

The response never includes the authentication code.

### Resend Lockout Response

```http
429 Too Many Requests
Retry-After: 240
```

```json
{
  "code": "AUTH_CODE_ALREADY_SENT",
  "message": "An authentication code was already sent. Try again after it expires.",
  "retryAfterSeconds": 240
}
```

### Error Responses

| Status | Code | When |
| --- | --- | --- |
| `400 Bad Request` | `MALFORMED_JSON` | Request body is not valid JSON or cannot be parsed. |
| `400 Bad Request` | `UNKNOWN_AUTHENTICATION_METHOD` | `method` is missing or unsupported. |
| `422 Unprocessable Entity` | `VALIDATION_FAILED` | Identifier or unknown-field validation failed. |
| `429 Too Many Requests` | `AUTH_CODE_ALREADY_SENT` | Active unconsumed code already exists for this user and method. |
| `500 Internal Server Error` | `AUTHENTICATION_DELIVERY_FAILED` | Delivery provider failed. |
| `501 Not Implemented` | `PHONE_DELIVERY_NOT_CONFIGURED` | Phone method selected but no phone provider is configured. |

## Verify Authentication Code

```http
POST /auth/token
```

Verifies an authentication code and returns a JWT access token.

### Request

```json
{
  "challengeId": "cha_01HX9M6VQ8Y7QK4Z3J5A2B1C0D",
  "code": "123456"
}
```

### Request Fields

| Field | Type | Required | Rules |
| --- | --- | --- | --- |
| `challengeId` | string | Yes | Existing authentication challenge id. |
| `code` | string | Yes | 6-digit numeric code. |

### Success Response

```http
200 OK
```

```json
{
  "tokenType": "Bearer",
  "accessToken": "jwt-token-value",
  "expiresAt": "2026-05-26T00:30:00Z"
}
```

The JWT includes:

- User id as subject.
- User type.
- Issued-at timestamp.
- Expiration timestamp.

### Error Responses

| Status | Code | When |
| --- | --- | --- |
| `400 Bad Request` | `MALFORMED_JSON` | Request body is not valid JSON or cannot be parsed. |
| `401 Unauthorized` | `INVALID_AUTHENTICATION_CODE` | Code is incorrect, expired, consumed, locked, or challenge does not exist. |
| `422 Unprocessable Entity` | `VALIDATION_FAILED` | Challenge id or code format validation failed. |
| `500 Internal Server Error` | `TOKEN_CREATION_FAILED` | JWT could not be created. |

## Normalization Rules

| Value | Rule |
| --- | --- |
| Email | Trim and lowercase. |
| Phone | Trim and normalize to digits. |
| CNPJ | Trim and normalize to digits only. |

## Security Notes

- Authentication codes expire after 5 minutes.
- Users cannot request another code for the same method while an active code exists.
- Authentication codes are stored as plain text by current project decision.
- Authentication codes are not returned by API responses.
- JWT tokens must not be logged.
- Console code delivery is intended only for local development and tests.
