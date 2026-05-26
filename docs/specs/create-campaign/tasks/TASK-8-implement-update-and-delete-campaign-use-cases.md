# TASK-8 — Implement Update And Delete Campaign Use Cases

**Target file:** `src/main/java/com/lhn/client_fidelity/application/campaign/ReplaceCampaignUseCase.java` (new)
**SDD reference:** Section 3.3
**Depends on:** TASK-5, TASK-7
**Blocked by:** TASK-5, TASK-7

---

## Context

The SDD adds full replace, partial update, and hard delete flows. These operations all require owner-scoped lookup first, validation against the same field rules as create, and a server-managed `updatedAt` refresh on every successful mutation.

## What to do

Create:

- `ReplaceCampaignCommand`
- `ReplaceCampaignUseCase`
- `PatchCampaignCommand`
- `PatchCampaignUseCase`
- `DeleteCampaignUseCase`
- `CampaignUpdateException`
- `CampaignDeletionException`

Apply owner-scoped load-first semantics:

```java
Campaign existing = campaignRepository.findByIdAndCommerceId(campaignId, commerceId)
        .orElseThrow(() -> new CampaignNotFoundException("Campaign was not found."));
```

For `PUT`, replace all editable fields from the request and preserve immutable ones:

```java
Campaign updated = new Campaign(
        existing.id(),
        existing.commerceId(),
        normalizedName,
        normalizedPoints,
        normalizedStartDate,
        normalizedExpirationDate,
        clock.instant(),
        normalizedTemplate
);
```

For `PATCH`, merge only present fields and reject an empty patch document. For `DELETE`, call the repository’s owner-scoped delete method and translate “nothing removed” into `CampaignNotFoundException`.

## Implementation notes

Use the same validation helpers as the create use case where practical. Reject `null` for patchable fields instead of treating `null` as “clear the field”, because the model has no nullable editable fields.

## Acceptance criteria

- [ ] `PUT` fully replaces editable campaign fields and refreshes `updatedAt`.
- [ ] `PATCH` updates only provided fields and rejects an empty patch.
- [ ] Final date state after replace or patch still enforces `expirationDate >= startDate`.
- [ ] `DELETE` hard-deletes owner-scoped campaigns and reports not found when appropriate.

