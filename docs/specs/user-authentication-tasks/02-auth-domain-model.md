# Task 02 - Auth Domain Model

## Goal

Model authentication challenges and code behavior.

## Scope

- Add `AuthenticationMethod` enum: `EMAIL`, `PHONE`.
- Add `AuthenticationChallenge`.
- Add `AuthenticationChallengeId`.
- Add secure 6-digit numeric code generation.
- Add expiration and consumed-state behavior.

## Acceptance Criteria

- Code is 6 numeric digits.
- Challenge knows whether it is expired.
- Challenge knows whether it is consumed.
- Plain-text code is stored on the challenge.
