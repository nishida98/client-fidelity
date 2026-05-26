# Task 13 - JWT Token Service

## Goal

Create JWT access tokens after successful authentication.

## Scope

- Add `TokenService` port.
- Add JWT implementation.
- Include claims:
  - subject user id
  - user type
  - issued-at
  - expiration
- Add signing key configuration.
- Add token expiration configuration, default 1 hour.

## Acceptance Criteria

- Successful verification returns a signed JWT.
- JWT expiration is configurable.
- Token creation failures are translated to `TokenCreationException`.
