# Task 19 - API Tests

## Goal

Verify authentication HTTP behavior.

## Scope

- Test `POST /auth/codes`.
- Test `POST /auth/token`.
- Test validation failures.
- Test unknown method.
- Test active challenge lockout.
- Test unknown user generic response.
- Test invalid code.
- Test delivery and token failure mappings.

## Acceptance Criteria

- API tests cover all HTTP statuses from the spec.
- Error responses match the shared error contract.
