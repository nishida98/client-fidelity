# TASK-5 — Add Campaign Domain And Repository Port

**Target file:** `src/main/java/com/lhn/client_fidelity/domain/campaign/Campaign.java` (new)
**SDD reference:** Sections 3.3 and 3.4
**Depends on:** none
**Blocked by:** none

---

## Context

There is no campaign model or persistence abstraction in the current codebase. The feature needs a stable domain representation and a repository contract before create, read, update, patch, and delete use cases can be implemented against H2 and CSV backends.

## What to do

Create the campaign domain package with `Campaign` and `CampaignId`, and add the `CampaignRepository` application port.

Use the domain shape from the SDD:

```java
public record Campaign(
        CampaignId id,
        UserId commerceId,
        String name,
        int points,
        LocalDate startDate,
        LocalDate expirationDate,
        Instant updatedAt,
        JsonNode template
) {
}
```

Add the repository contract:

```java
public interface CampaignRepository {

    Campaign save(Campaign campaign);

    Optional<Campaign> findByIdAndCommerceId(CampaignId campaignId, UserId commerceId);

    List<Campaign> findAllByCommerceId(UserId commerceId);

    void deleteByIdAndCommerceId(CampaignId campaignId, UserId commerceId);
}
```

Also create `CampaignId` with a static factory consistent with the project’s opaque id style.

## Implementation notes

The domain model does not validate raw request data directly; use cases do that. Keep `template` as a `JsonNode` so both REST and persistence layers can preserve opaque JSON without inventing a schema.

## Acceptance criteria

- [ ] The campaign domain package exists with `Campaign` and `CampaignId`.
- [ ] `CampaignRepository` supports owner-scoped lookup, list, save, and delete.
- [ ] No persistence implementation is added yet.

