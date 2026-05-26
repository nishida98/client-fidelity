# TASK-11 — Add Campaign Request DTOs

**Target file:** `src/main/java/com/lhn/client_fidelity/interfaces/rest/campaign/CreateCampaignRequest.java` (new)
**SDD reference:** Section 3.1
**Depends on:** TASK-6, TASK-8
**Blocked by:** TASK-6, TASK-8

---

## Context

The current request DTO pattern rejects unknown top-level fields explicitly, as shown in [CreateUserRequest.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/interfaces/rest/user/CreateUserRequest.java:12). Campaign routes need the same strict outer contract while still allowing arbitrary nested fields inside the opaque `template` object.

## What to do

Create:

- `CreateCampaignRequest`
- `ReplaceCampaignRequest`
- `PatchCampaignRequest`

For create and replace, use the same fixed fields:

```java
public final class CreateCampaignRequest {
    private String name;
    private Integer points;
    private String startDate;
    private String expirationDate;
    private JsonNode template;
    private final List<String> unknownFields = new ArrayList<>();
}
```

For patch, track field presence separately:

```java
public final class PatchCampaignRequest {
    private String name;
    private boolean namePresent;
    private Integer points;
    private boolean pointsPresent;
    private String startDate;
    private boolean startDatePresent;
    private String expirationDate;
    private boolean expirationDatePresent;
    private JsonNode template;
    private boolean templatePresent;
}
```

Each DTO should:

- reject unknown top-level fields with `CampaignValidationException`
- convert itself into the matching command type
- leave deep `template` validation to the use case, except confirming that the field is present where required

## Implementation notes

Do not accept `commerceId` or `updatedAt` in any request DTO. For patch, reject an empty request document during command conversion or in the use case, but keep the rule in one place consistently.

## Acceptance criteria

- [ ] Create and replace DTOs map to full commands.
- [ ] Patch DTO maps to a presence-aware partial command.
- [ ] Unknown top-level fields are rejected before controller execution continues.

