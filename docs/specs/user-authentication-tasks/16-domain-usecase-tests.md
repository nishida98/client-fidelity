# Task 16 - Domain And Use Case Tests

## Goal

Cover authentication business rules.

## Scope

- Test code generation.
- Test expiration.
- Test consumed behavior.
- Test resend lockout.
- Test unknown user response.
- Test successful verification.
- Test invalid, expired, consumed, reused, and too-many-attempt cases.

## Acceptance Criteria

- Authentication domain and use cases are covered by focused unit tests.
- Tests are deterministic by using injected `Clock`.
