# TASK-10 — Add CSV Campaign Persistence

**Target file:** `src/main/java/com/lhn/client_fidelity/infrastructure/campaign/csv/CsvCampaignRepository.java` (new)
**SDD reference:** Section 3.4
**Depends on:** TASK-5
**Blocked by:** TASK-5

---

## Context

The existing project supports CSV persistence as an alternative runtime mode in [application.yml](/C:/_development/client-fidelity/src/main/resources/application.yml:15), and users already have a CSV repository in [CsvUserRepository.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/infrastructure/user/csv/CsvUserRepository.java:20). Campaign support must keep that persistence option viable instead of silently becoming H2-only.

## What to do

Create:

- `CsvCampaignRecord`
- `CsvCampaignRepository`

Use a separate file, `data/campaigns.csv`, and persist the same fields as the H2 entity. The repository must implement:

- save/append
- `findByIdAndCommerceId`
- `findAllByCommerceId`
- `deleteByIdAndCommerceId`

Use a record helper similar to the user CSV pattern:

```java
record CsvCampaignRecord(
        String id,
        String commerceId,
        String name,
        int points,
        String startDate,
        String expirationDate,
        String updatedAt,
        String templatePayload
) {
}
```

## Implementation notes

Serialize `templatePayload` as compact JSON text. For delete and update flows, rewrite the full file safely after applying the owner-scoped change in memory. Keep synchronization semantics consistent with the existing CSV repository style.

## Acceptance criteria

- [ ] CSV persistence supports save, owner-scoped single read, owner-scoped list, and owner-scoped delete.
- [ ] The repository remains active only when `client-fidelity.persistence.type=csv`.
- [ ] Template payload survives round-trip serialization.

