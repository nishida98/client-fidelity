# Task 17 - Persistence Tests

## Goal

Verify authentication persistence behavior.

## Scope

- Test challenge save/load.
- Test active challenge lookup.
- Test consumed challenge exclusion.
- Test expired challenge exclusion.
- Test failed-attempt increment.
- Test user lookup changes in JPA and CSV adapters.

## Acceptance Criteria

- Persistence supports all auth use case requirements.
- JPA tests use real H2 behavior.
