# Task 15 - Error Handling

## Goal

Map authentication failures to stable HTTP responses.

## Scope

- Extend `RestExceptionHandler`.
- Add mappings for:
  - `UNKNOWN_AUTHENTICATION_METHOD`
  - `AUTH_CODE_ALREADY_SENT`
  - `INVALID_AUTHENTICATION_CODE`
  - `AUTHENTICATION_DELIVERY_FAILED`
  - `TOKEN_CREATION_FAILED`
  - `PHONE_DELIVERY_NOT_CONFIGURED`

## Acceptance Criteria

- Auth exceptions return the HTTP statuses defined by the spec.
- Error response shape is consistent with existing API errors.
