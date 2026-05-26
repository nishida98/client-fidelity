# Task 04 - Auth ORM Persistence

## Goal

Persist authentication challenges using JPA/H2.

## Scope

- Add `AuthenticationChallengeEntity`.
- Add Spring Data JPA repository.
- Add mapper between entity and domain.
- Add JPA adapter implementing `AuthenticationChallengeRepository`.
- Add indexes for:
  - `userId`
  - `method`
  - `expiresAt`
  - `consumedAt`

## Acceptance Criteria

- Challenges can be saved and loaded.
- Active challenge lookup works.
- Consumed and expired challenges are excluded from active lookup.
