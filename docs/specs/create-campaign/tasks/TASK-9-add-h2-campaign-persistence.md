# TASK-9 — Add H2 Campaign Persistence

**Target file:** `src/main/java/com/lhn/client_fidelity/infrastructure/campaign/h2/JpaCampaignRepository.java` (new)
**SDD reference:** Section 3.4
**Depends on:** TASK-5
**Blocked by:** TASK-5

---

## Context

The project defaults to H2 persistence through [application.yml](/C:/_development/client-fidelity/src/main/resources/application.yml:15). Campaign writes and owner-scoped reads therefore need a JPA-backed repository, entity mapping, and CRUD methods before the REST layer can be exercised against the default runtime mode.

## What to do

Create:

- `CampaignEntity`
- `JpaCampaignCrudRepository`
- `JpaCampaignRepository`

The entity must store:

- `id`
- `commerceId`
- `name`
- `points`
- `startDate`
- `expirationDate`
- `updatedAt`
- `templatePayload`

Expose owner-scoped repository methods:

```java
interface JpaCampaignCrudRepository extends JpaRepository<CampaignEntity, String> {

    Optional<CampaignEntity> findByIdAndCommerceId(String id, String commerceId);

    List<CampaignEntity> findAllByCommerceId(String commerceId);

    long deleteByIdAndCommerceId(String id, String commerceId);
}
```

Persist `templatePayload` as a JSON string and map it back to `JsonNode` in the adapter.

## Implementation notes

Add an index on `commerce_id`. Do not add name uniqueness. Treat JSON serialization/deserialization failures as infrastructure errors and wrap them in the campaign operation exceptions from the use-case layer.

## Acceptance criteria

- [ ] H2 persistence supports save, owner-scoped single read, owner-scoped list, and owner-scoped delete.
- [ ] Template payload is persisted as JSON text and restored as `JsonNode`.
- [ ] The repository remains active only when `client-fidelity.persistence.type=h2`.

