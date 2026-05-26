# Task 01 - Auth Foundation

## Goal

Create the base package structure and configuration surface for authentication.

## Scope

- Create authentication packages under:
  - `domain`
  - `application`
  - `infrastructure`
  - `interfaces`
- Add authentication exceptions under `com.lhn.client_fidelity.exception`.
- Add configuration keys for:
  - authentication code expiration
  - JWT expiration
  - code delivery provider selection

## Acceptance Criteria

- Authentication packages exist.
- Config values are externalized in `application.yml`.
- No business behavior is implemented yet.
