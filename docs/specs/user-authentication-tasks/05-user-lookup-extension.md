# Task 05 - User Lookup Extension

## Goal

Support user lookup by email and phone for authentication.

## Scope

- Extend `UserRepository` with:
  - find by normalized email
  - find by normalized phone
- Implement lookups in JPA adapter.
- Implement lookups in CSV adapter.

## Acceptance Criteria

- Email authentication can find users by normalized email.
- Phone authentication can find users by normalized phone.
- Unknown users can be handled without leaking account existence.
