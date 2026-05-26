# Task 12 - Verify Code Use Case

## Goal

Verify authentication code and issue JWT.

## Scope

- Validate challenge id.
- Validate code format.
- Load challenge.
- Reject unknown, expired, consumed, or locked challenge.
- Compare submitted code with stored plain-text code.
- Increment failed attempts on wrong code.
- Mark challenge consumed on success.
- Request JWT creation.

## Acceptance Criteria

- Valid code returns token result.
- Invalid code returns `InvalidAuthenticationCodeException`.
- Used code cannot be reused.
- Failed attempts are tracked.
