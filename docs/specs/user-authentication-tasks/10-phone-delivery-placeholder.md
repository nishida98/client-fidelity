# Task 10 - Phone Delivery Placeholder

## Goal

Represent phone authentication before choosing a concrete provider.

## Scope

- Add phone delivery provider placeholder.
- Return `PhoneDeliveryNotConfiguredException` when selected without a provider.
- Keep concrete SMS/WhatsApp provider as an open decision.

## Acceptance Criteria

- Phone method has a defined failure path.
- API can return `501 PHONE_DELIVERY_NOT_CONFIGURED`.
