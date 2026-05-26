# TASK-6 — Implement Create Campaign Use Case

**Target file:** `src/main/java/com/lhn/client_fidelity/application/campaign/CreateCampaignUseCase.java` (new)
**SDD reference:** Section 3.3
**Depends on:** TASK-1, TASK-5
**Blocked by:** TASK-1, TASK-5

---

## Context

The create flow is the first write path for campaigns. It depends on owner validation through `UserRepository.findById`, campaign-domain creation, and request validation rules for `name`, `points`, `startDate`, `expirationDate`, and `template`.

## What to do

Create the command, result, use case, and create-time exception types:

- `CreateCampaignCommand`
- `CreateCampaignResult`
- `CreateCampaignUseCase`
- `CampaignValidationException`
- `CampaignCreationException`

Implement the use case around this shape:

```java
public CreateCampaignResult execute(CreateCampaignCommand command) {
    User owner = userRepository.findById(new UserId(command.commerceId()))
            .orElseThrow(() -> new InvalidAccessTokenException("Access token is invalid or expired."));

    if (owner.type() != UserType.COMMERCE) {
        throw new ForbiddenUserTypeException("Only commerce users can create campaigns.");
    }

    // validate name, points, dates, template
    // set updatedAt from Clock
    // save through CampaignRepository
}
```

Validation rules:

- blank `name` rejected
- `points < 0` rejected
- invalid date format rejected
- `expirationDate.isBefore(startDate)` rejected
- `template == null` or non-object template rejected

## Implementation notes

Keep date fields as strings in the command and parse them inside the use case so invalid formats map to `422 VALIDATION_FAILED` later. Wrap unexpected save failures in `CampaignCreationException` only after validation is complete.

## Acceptance criteria

- [ ] Valid commerce-owned create commands produce a persisted campaign result.
- [ ] `points = 0` is accepted.
- [ ] Invalid fields raise `CampaignValidationException`.
- [ ] Non-`COMMERCE` owners raise `ForbiddenUserTypeException`.

