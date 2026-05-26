# Task 08 - SMTP Email Delivery

## Goal

Prepare email delivery through SMTP.

## Scope

- Add SMTP delivery provider.
- Add SMTP configuration:
  - host
  - port
  - username
  - password
  - sender
  - TLS setting
  - timeout
- Add Spring Mail dependency if implementing real SMTP sending.
- Translate provider failures.

## Acceptance Criteria

- SMTP provider can send authentication codes.
- Secrets are externalized.
- Delivery failures become `AuthenticationDeliveryFailedException`.
