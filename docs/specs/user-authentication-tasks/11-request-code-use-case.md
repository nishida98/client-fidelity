# Task 11 - Request Code Use Case

## Goal

Implement code request flow.

## Scope

- Validate authentication method.
- Normalize email or phone.
- Find user by identifier.
- Return accepted result for unknown users without sending a code.
- Enforce active-code resend lockout.
- Generate plain-text 6-digit code.
- Persist challenge with 5-minute expiration.
- Send code through configured provider.

## Acceptance Criteria

- Existing users receive a code.
- Unknown users get a generic accepted response.
- Active challenge blocks resend until expiration.
- Expired or consumed challenge allows a new code.
