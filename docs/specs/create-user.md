# Create User Specification

## Purpose

Create a user record for the loyalty platform.

The system supports two user types:

- `COMMERCE`: a business that owns a loyalty program.
- `COMMERCE_CLIENT`: a customer of a commerce.

The feature must reject duplicates before insertion. Duplicate checks are part of the use case, not only a database constraint.

## Endpoint

```http
POST /users
Content-Type: application/json
Accept: application/json
```

## Request Contract

The request uses `type` to decide which required fields apply.

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

```json
{
  "type": "COMMERCE_CLIENT",
  "name": "Joao Souza",
  "email": "joao@email.com",
  "phone": "+5511988888888"
}
```

### Fields

| Field | Type | Required for `COMMERCE` | Required for `COMMERCE_CLIENT` | Rules |
| --- | --- | --- | --- | --- |
| `type` | string | Yes | Yes | Must be `COMMERCE` or `COMMERCE_CLIENT`. |
| `name` | string | Yes | Yes | Must not be blank after trimming. |
| `contactName` | string | Yes | No | Must not be blank for commerce. Must be ignored or rejected for commerce clients. Prefer rejecting with validation error to keep the contract strict. |
| `email` | string | Yes | Yes | Must not be blank. Must pass email validation. Store and compare in normalized lowercase form. |
| `phone` | string | Yes | Yes | Must not be blank. Must pass phone validation. Store in normalized form. |
| `governmentIdentifier` | string | Yes | No | Required for commerce. In Brazil, this is the CNPJ. Must pass government identifier validation. Must be normalized to digits only before validation and duplicate lookup. Must be rejected for commerce clients. |

### Normalization

Before validation and duplicate lookup:

- Trim all string fields.
- Normalize `email` to lowercase.
- Normalize `governmentIdentifier` to digits only.
- Normalize `phone` to the agreed persisted format before saving.

The response must not expose a differently interpreted value than the one persisted.

## Input Validation

Validation happens after trimming and normalization.

### Email

Email validation must reject:

- Blank values.
- Values without a local part.
- Values without a domain.
- Values without a top-level domain.
- Values with spaces.
- Values with multiple `@` characters.

Examples:

| Input | Result |
| --- | --- |
| `customer@email.com` | Valid |
| `CUSTOMER@EMAIL.COM` | Valid, normalized to `customer@email.com` |
| `customer@` | Invalid |
| `@email.com` | Invalid |
| `customeremail.com` | Invalid |
| `customer@localhost` | Invalid |
| `customer @email.com` | Invalid |

### Phone

Phone validation must reject:

- Blank values.
- Values with letters.
- Values with unsupported symbols.
- Values with fewer than 10 digits after normalization.
- Values with more than 15 digits after normalization.

Phone values may contain spaces, parentheses, hyphens, and a leading `+` in the request, but must be normalized before persistence.

Examples:

| Input | Result |
| --- | --- |
| `+5511999999999` | Valid |
| `(11) 99999-9999` | Valid |
| `11999999999` | Valid |
| `12345` | Invalid |
| `+55 11 phone` | Invalid |
| `+55@11999999999` | Invalid |

### Government Identifier

For `COMMERCE`, the government identifier is the Brazilian CNPJ.

CNPJ validation must:

- Normalize the value to digits only before validation.
- Require exactly 14 digits after normalization.
- Reject values with all digits equal, such as `00000000000000` or `11111111111111`.
- Validate the CNPJ check digits.

Examples:

| Input | Result |
| --- | --- |
| `12.345.678/0001-95` | Valid if check digits are correct, normalized to `12345678000195` |
| `12345678000195` | Valid if check digits are correct |
| `1234567800019` | Invalid |
| `123456780001950` | Invalid |
| `00000000000000` | Invalid |
| `12.345.678/0001-00` | Invalid if check digits are incorrect |

## Duplicate Rules

### Commerce

A commerce is duplicate when another commerce already exists with the same normalized `governmentIdentifier`.

The use case must query for an existing commerce by government identifier before attempting insertion.

### Commerce Client

A commerce client is duplicate when another commerce client already exists with the same normalized `email`.

The use case must query for an existing commerce client by email before attempting insertion.

### Race Conditions

The application-level duplicate check is required for clear error handling, but persistence must still enforce uniqueness with a database constraint when persistence is implemented.

If insertion fails because of a unique constraint race, translate it to the same duplicate response used by the pre-insertion duplicate check.

## Successful Response

### Status

```http
201 Created
Location: /users/{userId}
```

### Body

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

Commerce client responses must not include `contactName` or `governmentIdentifier`.

## Error Response Contract

Errors use a consistent object:

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

`details` is optional for non-validation errors.

## HTTP Statuses

| Status | Code | When |
| --- | --- | --- |
| `201 Created` | N/A | User was created. |
| `400 Bad Request` | `MALFORMED_JSON` | Request body is not valid JSON or cannot be parsed. |
| `400 Bad Request` | `UNKNOWN_USER_TYPE` | `type` is missing or is not `COMMERCE` or `COMMERCE_CLIENT`. |
| `422 Unprocessable Entity` | `VALIDATION_FAILED` | Request JSON is valid, but one or more fields fail business validation. |
| `409 Conflict` | `COMMERCE_ALREADY_EXISTS` | A commerce already exists with the same normalized government identifier. |
| `409 Conflict` | `COMMERCE_CLIENT_ALREADY_EXISTS` | A commerce client already exists with the same normalized email. |
| `500 Internal Server Error` | `USER_CREATION_FAILED` | Unexpected failure. Do not expose internal details. |

## Exceptions

| Exception | HTTP Status | Error Code | Response Message |
| --- | --- | --- | --- |
| `MalformedRequestException` | `400 Bad Request` | `MALFORMED_JSON` | `Request body must be valid JSON.` |
| `UnknownUserTypeException` | `400 Bad Request` | `UNKNOWN_USER_TYPE` | `User type must be COMMERCE or COMMERCE_CLIENT.` |
| `UserValidationException` | `422 Unprocessable Entity` | `VALIDATION_FAILED` | `The request contains invalid fields.` |
| `CommerceAlreadyExistsException` | `409 Conflict` | `COMMERCE_ALREADY_EXISTS` | `Commerce already exists for the provided government identifier.` |
| `CommerceClientAlreadyExistsException` | `409 Conflict` | `COMMERCE_CLIENT_ALREADY_EXISTS` | `Commerce client already exists for the provided email.` |
| `UserCreationException` | `500 Internal Server Error` | `USER_CREATION_FAILED` | `User could not be created.` |

## Edge Cases

### Common Validation

- Missing body returns `400 MALFORMED_JSON`.
- Empty JSON object returns `400 UNKNOWN_USER_TYPE` because the use case cannot choose the contract branch.
- Unknown `type` returns `400 UNKNOWN_USER_TYPE`.
- Blank `name` returns `422 VALIDATION_FAILED`.
- Blank `email` returns `422 VALIDATION_FAILED`.
- Invalid `email` format returns `422 VALIDATION_FAILED`.
- Blank `phone` returns `422 VALIDATION_FAILED`.
- Invalid `phone` returns `422 VALIDATION_FAILED`.
- Extra unknown fields should be rejected with `422 VALIDATION_FAILED` unless the project later standardizes lenient request parsing.

### Commerce Validation

- Missing `contactName` returns `422 VALIDATION_FAILED`.
- Blank `contactName` returns `422 VALIDATION_FAILED`.
- Missing `governmentIdentifier` returns `422 VALIDATION_FAILED`.
- Blank `governmentIdentifier` returns `422 VALIDATION_FAILED`.
- Invalid CNPJ returns `422 VALIDATION_FAILED`.
- CNPJ with punctuation must be normalized before validation and duplicate lookup.
- Existing commerce with same normalized CNPJ returns `409 COMMERCE_ALREADY_EXISTS`.
- Same email as another commerce is allowed unless a future global-email uniqueness rule is introduced.
- Same email as a commerce client is allowed unless a future global-email uniqueness rule is introduced.

### Commerce Client Validation

- Missing `contactName` is valid.
- Present `contactName` returns `422 VALIDATION_FAILED`.
- Missing `governmentIdentifier` is valid.
- Present `governmentIdentifier` returns `422 VALIDATION_FAILED`.
- Existing commerce client with same normalized email returns `409 COMMERCE_CLIENT_ALREADY_EXISTS`.
- Same email as a commerce is allowed unless a future global-email uniqueness rule is introduced.

## Use Case Flow

1. Parse request body.
2. Validate `type`.
3. Normalize fields.
4. Validate fields required for the selected type.
5. Check duplicate existence:
   - Commerce: lookup by normalized government identifier.
   - Commerce client: lookup by normalized email.
6. If duplicate exists, return `409 Conflict`.
7. Create the user.
8. Return `201 Created` with response body and `Location` header.

## Test Coverage

Tests must cover all observable use cases. Prefer unit tests for the use case and focused controller tests for HTTP mapping.

### Use Case Tests

| Scenario | Expected Result |
| --- | --- |
| Creates commerce with valid request | Returns created commerce response. |
| Creates commerce client with valid request | Returns created commerce client response. |
| Normalizes commerce CNPJ before duplicate lookup | Repository lookup receives digits-only CNPJ. |
| Normalizes email before commerce client duplicate lookup | Repository lookup receives lowercase email. |
| Rejects duplicate commerce by CNPJ | Throws `CommerceAlreadyExistsException`. |
| Rejects duplicate commerce client by email | Throws `CommerceClientAlreadyExistsException`. |
| Rejects missing type | Throws `UnknownUserTypeException`. |
| Rejects unknown type | Throws `UnknownUserTypeException`. |
| Rejects blank commerce name | Throws `UserValidationException`. |
| Rejects blank commerce client name | Throws `UserValidationException`. |
| Accepts uppercase email and normalizes it | Created user uses lowercase email. |
| Rejects email without local part | Throws `UserValidationException`. |
| Rejects email without domain | Throws `UserValidationException`. |
| Rejects email without top-level domain | Throws `UserValidationException`. |
| Rejects email with spaces | Throws `UserValidationException`. |
| Rejects email with multiple `@` characters | Throws `UserValidationException`. |
| Rejects invalid email | Throws `UserValidationException`. |
| Accepts phone with spaces, parentheses, hyphens, or leading `+` | Created user uses normalized phone. |
| Rejects phone with letters | Throws `UserValidationException`. |
| Rejects phone with unsupported symbols | Throws `UserValidationException`. |
| Rejects phone with fewer than 10 digits | Throws `UserValidationException`. |
| Rejects phone with more than 15 digits | Throws `UserValidationException`. |
| Rejects invalid phone | Throws `UserValidationException`. |
| Rejects commerce without contact name | Throws `UserValidationException`. |
| Rejects commerce without government identifier | Throws `UserValidationException`. |
| Accepts punctuated valid CNPJ and normalizes it | Created commerce uses digits-only government identifier. |
| Rejects CNPJ with fewer than 14 digits | Throws `UserValidationException`. |
| Rejects CNPJ with more than 14 digits | Throws `UserValidationException`. |
| Rejects CNPJ with all digits equal | Throws `UserValidationException`. |
| Rejects CNPJ with invalid check digits | Throws `UserValidationException`. |
| Rejects commerce with invalid CNPJ | Throws `UserValidationException`. |
| Rejects commerce client with contact name | Throws `UserValidationException`. |
| Rejects commerce client with government identifier | Throws `UserValidationException`. |
| Translates unique constraint race for commerce | Throws `CommerceAlreadyExistsException`. |
| Translates unique constraint race for commerce client | Throws `CommerceClientAlreadyExistsException`. |

### Controller/API Tests

| Scenario | Expected HTTP Response |
| --- | --- |
| Valid commerce request | `201 Created`, `Location` header, commerce response body. |
| Valid commerce client request | `201 Created`, `Location` header, commerce client response body. |
| Malformed JSON | `400 MALFORMED_JSON`. |
| Missing type | `400 UNKNOWN_USER_TYPE`. |
| Unknown type | `400 UNKNOWN_USER_TYPE`. |
| Validation failure | `422 VALIDATION_FAILED` with field details. |
| Duplicate commerce | `409 COMMERCE_ALREADY_EXISTS`. |
| Duplicate commerce client | `409 COMMERCE_CLIENT_ALREADY_EXISTS`. |
| Unexpected use case failure | `500 USER_CREATION_FAILED` without internal details. |

## Open Decisions

- Exact phone validation rules must be finalized before implementation. The request contract requires a valid phone number, but the accepted countries/formats are not yet defined.
- The project must decide whether unknown JSON fields are globally rejected or ignored. This spec recommends rejecting them for strict API contracts.
- The project must decide the final ID format. The examples use opaque IDs and do not require sequential identifiers.
