# User Authentication Specification

## Purpose

Authenticate existing users with a short-lived verification code sent through the selected authentication method.

After the code is validated, the application returns a JWT access token that clients use to authenticate future requests.

## Authentication Model

Authentication is a two-step flow:

1. Request an authentication code.
2. Verify the code and receive a JWT token.

The endpoint receives the authentication method, and the application selects the correct delivery implementation.

Supported authentication methods:

- `EMAIL`
- `PHONE`

Supported email delivery providers:

- Local console provider for local development and tests.
- SMTP provider.
- Amazon SES provider.

Phone delivery is intentionally defined as a port/interface only. The concrete provider must be selected later.

## Storage Decision

Authentication codes must be saved in the database, not only in an in-memory cache.

Reasoning:

- Codes must survive application restarts during the 5-minute validity window.
- Multiple application instances must see the same code state.
- Resend lockout must be consistent across instances.
- Successful and failed attempts should be auditable enough for abuse investigation.

A cache may be added later as an optimization, but the database remains the source of truth.

Required persistence fields:

| Field | Purpose |
| --- | --- |
| `id` | Opaque authentication challenge id. |
| `userId` | User being authenticated. |
| `method` | `EMAIL` or `PHONE`. |
| `destination` | Normalized email or phone used for delivery. |
| `code` | Plain-text generated code. |
| `expiresAt` | Code expiration timestamp. |
| `consumedAt` | Timestamp set after successful verification. |
| `attemptCount` | Failed verification attempts. |
| `createdAt` | Creation timestamp. |

The 5-minute resend lockout is enforced by querying active, unconsumed challenges for the same user and method.

## Endpoint: Request Code

```http
POST /auth/codes
Content-Type: application/json
Accept: application/json
```

### Request

```json
{
  "method": "EMAIL",
  "identifier": "customer@email.com"
}
```

```json
{
  "method": "PHONE",
  "identifier": "+5511999999999"
}
```

### Fields

| Field | Type | Required | Rules |
| --- | --- | --- | --- |
| `method` | string | Yes | Must be `EMAIL` or `PHONE`. |
| `identifier` | string | Yes | Email for `EMAIL`; phone for `PHONE`. Must be normalized before lookup. |

### Successful Response

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

The response must not include the code.

For local development only, the console delivery provider prints the code to application logs or console output.

## Endpoint: Verify Code

```http
POST /auth/token
Content-Type: application/json
Accept: application/json
```

### Request

```json
{
  "challengeId": "cha_01HX9M6VQ8Y7QK4Z3J5A2B1C0D",
  "code": "123456"
}
```

### Fields

| Field | Type | Required | Rules |
| --- | --- | --- | --- |
| `challengeId` | string | Yes | Must reference an existing authentication challenge. |
| `code` | string | Yes | Must be a 6-digit numeric code. |

### Successful Response

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

The JWT must include:

- User id as subject.
- User type.
- Issued-at timestamp.
- Expiration timestamp.

Recommended JWT expiration: 1 hour.

## Delivery Providers

### Provider Port

Authentication code delivery must depend on a port/interface, for example:

```text
AuthenticationCodeDelivery
```

Expected behavior:

- `supports(method, provider)`
- `send(destination, code)`

The application use case chooses the implementation based on method and configuration.

### Local Console Email Provider

Use for local development and tests.

Behavior:

- Supports `EMAIL`.
- Prints the generated code and destination to the console.
- Must never be enabled in production.

### SMTP Email Provider

Use for standard email delivery.

Behavior:

- Supports `EMAIL`.
- Sends the code using configured SMTP host, port, username, password, sender, TLS setting, and timeout.
- Must not log SMTP credentials or the plain code in production logs.

### Amazon SES Email Provider

Use for AWS email delivery.

Behavior:

- Supports `EMAIL`.
- Sends the code using Amazon SES.
- Requires region, sender identity, and AWS credentials supplied by environment or IAM role.
- Must use request timeouts and translate provider failures into application-specific delivery failures.

### Phone Provider

Use for SMS, WhatsApp, or another phone delivery provider in the future.

Behavior:

- Supports `PHONE`.
- The concrete integration is not defined yet.
- Until selected, phone authentication may return `501 PHONE_DELIVERY_NOT_CONFIGURED` or be disabled by configuration.

## Code Rules

- Code must be 6 numeric digits.
- Code expires 5 minutes after creation.
- Code must be random and generated with a cryptographically secure random generator.
- Plain code is persisted in the authentication challenge record.
- Plain code must not be returned by API responses.
- Code can be used only once.
- After successful verification, mark the challenge as consumed.
- Verification after `expiresAt` must fail.
- Verification after `consumedAt` must fail.

## Resend Lockout

A user cannot request a new code for the same method while an active code exists.

An active code is:

- Not consumed.
- Not expired.
- Same user.
- Same authentication method.

If an active code exists, return:

```http
429 Too Many Requests
```

```json
{
  "code": "AUTH_CODE_ALREADY_SENT",
  "message": "An authentication code was already sent. Try again after it expires.",
  "retryAfterSeconds": 240
}
```

The response should include `Retry-After` with the number of seconds until a new code can be requested.

## User Lookup

For `EMAIL`:

- Normalize and validate the email.
- Find user by normalized email.

For `PHONE`:

- Normalize and validate the phone.
- Find user by normalized phone.

If no user exists, return a generic response that does not reveal whether the account exists.

Recommended behavior:

- Return `202 Accepted` for unknown users on code request.
- Do not send a code.
- Do not create a challenge.

This avoids user enumeration.

## Error Response Contract

Errors use a consistent object:

```json
{
  "code": "VALIDATION_FAILED",
  "message": "The request contains invalid fields.",
  "details": [
    {
      "field": "identifier",
      "message": "Email must be valid."
    }
  ]
}
```

`details` is optional for non-validation errors.

## HTTP Statuses

| Status | Code | When |
| --- | --- | --- |
| `200 OK` | N/A | Code was verified and JWT was issued. |
| `202 Accepted` | N/A | Code request was accepted. Also used for unknown users to avoid enumeration. |
| `400 Bad Request` | `MALFORMED_JSON` | Request body is not valid JSON or cannot be parsed. |
| `400 Bad Request` | `UNKNOWN_AUTHENTICATION_METHOD` | `method` is missing or is not supported. |
| `401 Unauthorized` | `INVALID_AUTHENTICATION_CODE` | Code is incorrect, expired, consumed, or challenge does not exist. |
| `422 Unprocessable Entity` | `VALIDATION_FAILED` | Request JSON is valid, but fields fail validation. |
| `429 Too Many Requests` | `AUTH_CODE_ALREADY_SENT` | Active code already exists for the same user and method. |
| `500 Internal Server Error` | `AUTHENTICATION_DELIVERY_FAILED` | Delivery provider failed unexpectedly. |
| `500 Internal Server Error` | `TOKEN_CREATION_FAILED` | JWT could not be created. |
| `501 Not Implemented` | `PHONE_DELIVERY_NOT_CONFIGURED` | Phone method is selected but no provider is configured. |

## Exceptions

| Exception | HTTP Status | Error Code | Response Message |
| --- | --- | --- | --- |
| `UnknownAuthenticationMethodException` | `400 Bad Request` | `UNKNOWN_AUTHENTICATION_METHOD` | `Authentication method must be EMAIL or PHONE.` |
| `AuthenticationValidationException` | `422 Unprocessable Entity` | `VALIDATION_FAILED` | `The request contains invalid fields.` |
| `AuthenticationCodeAlreadySentException` | `429 Too Many Requests` | `AUTH_CODE_ALREADY_SENT` | `An authentication code was already sent. Try again after it expires.` |
| `InvalidAuthenticationCodeException` | `401 Unauthorized` | `INVALID_AUTHENTICATION_CODE` | `Authentication code is invalid or expired.` |
| `AuthenticationDeliveryFailedException` | `500 Internal Server Error` | `AUTHENTICATION_DELIVERY_FAILED` | `Authentication code could not be delivered.` |
| `TokenCreationException` | `500 Internal Server Error` | `TOKEN_CREATION_FAILED` | `Authentication token could not be created.` |
| `PhoneDeliveryNotConfiguredException` | `501 Not Implemented` | `PHONE_DELIVERY_NOT_CONFIGURED` | `Phone authentication delivery is not configured.` |

## Edge Cases

### Request Code

- Missing body returns `400 MALFORMED_JSON`.
- Empty JSON returns `400 UNKNOWN_AUTHENTICATION_METHOD`.
- Unknown `method` returns `400 UNKNOWN_AUTHENTICATION_METHOD`.
- Blank `identifier` returns `422 VALIDATION_FAILED`.
- Invalid email for `EMAIL` returns `422 VALIDATION_FAILED`.
- Invalid phone for `PHONE` returns `422 VALIDATION_FAILED`.
- Existing active code for same user and method returns `429 AUTH_CODE_ALREADY_SENT`.
- Existing expired code allows creation of a new code.
- Existing consumed code allows creation of a new code.
- Unknown user returns `202 Accepted` without sending a code.
- Delivery provider failure returns `500 AUTHENTICATION_DELIVERY_FAILED`.
- Phone method without configured provider returns `501 PHONE_DELIVERY_NOT_CONFIGURED`.
- Unknown JSON fields return `422 VALIDATION_FAILED`.

### Verify Code

- Missing body returns `400 MALFORMED_JSON`.
- Blank `challengeId` returns `422 VALIDATION_FAILED`.
- Blank `code` returns `422 VALIDATION_FAILED`.
- Non-numeric code returns `422 VALIDATION_FAILED`.
- Code with fewer or more than 6 digits returns `422 VALIDATION_FAILED`.
- Unknown `challengeId` returns `401 INVALID_AUTHENTICATION_CODE`.
- Expired challenge returns `401 INVALID_AUTHENTICATION_CODE`.
- Consumed challenge returns `401 INVALID_AUTHENTICATION_CODE`.
- Incorrect code returns `401 INVALID_AUTHENTICATION_CODE`.
- Correct code marks challenge as consumed and returns JWT.
- Reusing the same code after success returns `401 INVALID_AUTHENTICATION_CODE`.
- JWT creation failure returns `500 TOKEN_CREATION_FAILED`.

### Abuse Controls

- Store `attemptCount`.
- After too many failed attempts, invalidate or lock the challenge.
- Recommended maximum failed attempts: 5.
- When the attempt limit is reached, return `401 INVALID_AUTHENTICATION_CODE`.
- Do not reveal whether the code was wrong, expired, consumed, or locked.
- Do not log authentication codes except through the local console provider.
- Never log JWT tokens.

## Use Case Flow

### Request Code

1. Parse request body.
2. Validate authentication method.
3. Normalize and validate identifier.
4. Find user by email or phone.
5. If user does not exist, return `202 Accepted` without sending a code.
6. Check for active unconsumed challenge for the same user and method.
7. If active challenge exists, return `429 Too Many Requests`.
8. Generate a secure 6-digit code.
9. Persist challenge with plain-text code and 5-minute expiration.
11. Deliver code using the configured provider.
12. Return `202 Accepted`.

### Verify Code

1. Parse request body.
2. Validate `challengeId` and `code`.
3. Load challenge.
4. Reject unknown, expired, consumed, or locked challenge.
5. Compare submitted code with stored plain-text code.
6. Increment failed attempt count when code is wrong.
7. Mark challenge as consumed when code is correct.
8. Create JWT token.
9. Return `200 OK`.

## Test Coverage

### Domain/Application Tests

| Scenario | Expected Result |
| --- | --- |
| Requests email code for existing user | Persists challenge and sends code through email provider. |
| Requests phone code for existing user | Uses phone delivery provider when configured. |
| Requests code for unknown user | Returns accepted result without sending code. |
| Rejects unknown method | Throws `UnknownAuthenticationMethodException`. |
| Rejects invalid email | Throws `AuthenticationValidationException`. |
| Rejects invalid phone | Throws `AuthenticationValidationException`. |
| Rejects active duplicate code request | Throws `AuthenticationCodeAlreadySentException`. |
| Allows new code after expiration | Persists and sends a new challenge. |
| Allows new code after previous code was consumed | Persists and sends a new challenge. |
| Stores plain-text code | Persisted challenge contains the generated code. |
| Verifies correct code before expiration | Returns JWT result and consumes challenge. |
| Rejects incorrect code | Throws `InvalidAuthenticationCodeException`. |
| Rejects expired code | Throws `InvalidAuthenticationCodeException`. |
| Rejects consumed code | Throws `InvalidAuthenticationCodeException`. |
| Rejects code reuse | Throws `InvalidAuthenticationCodeException`. |
| Rejects too many failed attempts | Throws `InvalidAuthenticationCodeException`. |
| Delivery provider failure | Throws `AuthenticationDeliveryFailedException`. |
| JWT creation failure | Throws `TokenCreationException`. |

### Provider Tests

| Scenario | Expected Result |
| --- | --- |
| Console email provider sends code | Writes destination and code to console output. |
| SMTP email provider sends code | Calls SMTP client with expected message. |
| SES email provider sends code | Calls SES client with expected request. |
| Phone provider not configured | Throws `PhoneDeliveryNotConfiguredException`. |

### Controller/API Tests

| Scenario | Expected HTTP Response |
| --- | --- |
| Valid email code request | `202 Accepted`. |
| Valid phone code request | `202 Accepted` or `501 PHONE_DELIVERY_NOT_CONFIGURED` depending on config. |
| Unknown method | `400 UNKNOWN_AUTHENTICATION_METHOD`. |
| Validation failure | `422 VALIDATION_FAILED`. |
| Active code exists | `429 AUTH_CODE_ALREADY_SENT` and `Retry-After` header. |
| Valid code verification | `200 OK` with JWT response. |
| Invalid code verification | `401 INVALID_AUTHENTICATION_CODE`. |
| Delivery failure | `500 AUTHENTICATION_DELIVERY_FAILED`. |
| Token creation failure | `500 TOKEN_CREATION_FAILED`. |

## Open Decisions

- Select the concrete phone delivery provider.
- Decide whether phone authentication should be disabled until the provider is selected or return `501 PHONE_DELIVERY_NOT_CONFIGURED`.
- Decide JWT signing algorithm and key management approach.
- Decide refresh-token support. This spec only defines access-token issuance.
- Decide whether authentication codes should be invalidated when a newer code is issued after expiration.
