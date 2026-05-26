# Task 03 - Auth Persistence Port

## Goal

Define persistence operations needed by authentication use cases.

## Scope

- Add `AuthenticationChallengeRepository` application port.
- Include methods to:
  - save challenge
  - find challenge by id
  - find active challenge by user and method
  - mark challenge as consumed
  - increment failed attempts

## Acceptance Criteria

- Use cases can depend on the port without knowing persistence details.
- Repository API supports resend lockout and verification attempt tracking.
