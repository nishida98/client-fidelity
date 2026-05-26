# Task 09 - Amazon SES Email Delivery

Status: provider placeholder implemented. Real SES sending still requires selecting and configuring the AWS SES client dependency and credentials strategy.

## Goal

Prepare email delivery through Amazon SES.

## Scope

- Add SES delivery provider.
- Add SES configuration:
  - region
  - sender identity
  - credentials source
  - timeout
- Add AWS SES dependency only when implementing real SES sending.
- Translate provider failures.

## Acceptance Criteria

- SES provider can send authentication codes.
- Credentials are externalized or resolved by IAM role.
- Delivery failures become `AuthenticationDeliveryFailedException`.
