# Task 06 - Delivery Port

## Goal

Abstract authentication code delivery.

## Scope

- Add `AuthenticationCodeDelivery` interface.
- Define delivery input:
  - method
  - destination
  - code
- Add provider selection strategy based on method and configuration.

## Acceptance Criteria

- Use cases depend on a delivery port.
- Delivery providers are replaceable.
- Unsupported delivery methods fail through a domain-specific exception.
