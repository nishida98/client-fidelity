# Task 14 - REST Contracts

## Goal

Expose authentication endpoints.

## Scope

- Add `POST /auth/codes`.
- Add `POST /auth/token`.
- Add request DTOs.
- Add response DTOs.
- Reject unknown JSON fields.
- Return `Retry-After` for resend lockout.

## Acceptance Criteria

- Code request returns `202 Accepted`.
- Code verification returns `200 OK` with JWT.
- Response bodies match the spec.
