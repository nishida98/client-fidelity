# TASK-13 — Extend Rest Exception Handler For Campaigns

**Target file:** `src/main/java/com/lhn/client_fidelity/interfaces/rest/error/RestExceptionHandler.java` (existing)
**SDD reference:** Section 3.5
**Depends on:** TASK-2, TASK-6, TASK-7, TASK-8
**Blocked by:** TASK-2, TASK-6, TASK-7, TASK-8

---

## Context

[RestExceptionHandler.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/interfaces/rest/error/RestExceptionHandler.java:26) already centralizes API error mapping for user creation and authentication flows. Campaign endpoints need to reuse the same shared error shape for `401`, `403`, `404`, `422`, and `500` outcomes, rather than returning framework-default responses.

## What to do

Add exception mappings for:

- `MissingAccessTokenException` -> `401 MISSING_ACCESS_TOKEN`
- `InvalidAccessTokenException` -> `401 INVALID_ACCESS_TOKEN`
- `ForbiddenUserTypeException` -> `403 FORBIDDEN_USER_TYPE`
- `CampaignNotFoundException` -> `404 CAMPAIGN_NOT_FOUND`
- `CampaignValidationException` -> `422 VALIDATION_FAILED`
- `CampaignCreationException` -> `500 CAMPAIGN_CREATION_FAILED`
- `CampaignUpdateException` -> `500 CAMPAIGN_UPDATE_FAILED`
- `CampaignDeletionException` -> `500 CAMPAIGN_DELETION_FAILED`

Follow the existing pattern:

```java
@ExceptionHandler(CampaignNotFoundException.class)
ResponseEntity<ErrorResponse> handleCampaignNotFound(CampaignNotFoundException exception) {
    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.withoutDetails("CAMPAIGN_NOT_FOUND", exception.getMessage()));
}
```

Map `CampaignValidationException` with field details the same way `UserValidationException` is handled today.

## Implementation notes

Do not change existing user or authentication mappings. Keep non-field errors on the compact `{ code, message }` shape and field-validation errors on `{ code, message, details }`.

## Acceptance criteria

- [ ] Campaign access-token, not-found, validation, update, create, and delete exceptions are mapped.
- [ ] Error payloads remain consistent with the shared API contract.
- [ ] Existing error mappings still behave as before.

