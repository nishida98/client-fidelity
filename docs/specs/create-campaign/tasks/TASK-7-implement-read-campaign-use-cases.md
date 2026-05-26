# TASK-7 — Implement Read Campaign Use Cases

**Target file:** `src/main/java/com/lhn/client_fidelity/application/campaign/GetCampaignByIdUseCase.java` (new)
**SDD reference:** Section 3.3
**Depends on:** TASK-5
**Blocked by:** TASK-5

---

## Context

The SDD adds owner-scoped read routes for a single campaign and for all campaigns owned by the authenticated commerce. Both flows must avoid leaking campaign existence across commerces, which means a missing record and an out-of-scope record both resolve to the same not-found result.

## What to do

Create:

- `GetCampaignByIdUseCase`
- `ListCommerceCampaignsUseCase`
- `CampaignNotFoundException`

Use the repository’s owner-scoped methods directly:

```java
public CampaignResponseModel execute(String campaignId, String commerceId) {
    return campaignRepository.findByIdAndCommerceId(new CampaignId(campaignId), new UserId(commerceId))
            .map(CampaignResponseModel::from)
            .orElseThrow(() -> new CampaignNotFoundException("Campaign was not found."));
}
```

For the list flow:

```java
public List<CampaignResponseModel> execute(String commerceId) {
    return campaignRepository.findAllByCommerceId(new UserId(commerceId)).stream()
            .map(CampaignResponseModel::from)
            .toList();
}
```

You may reuse `CreateCampaignResult` as the response model if it keeps the API surface simple; otherwise add a shared `CampaignView`.

## Implementation notes

Do not add pagination, filters, or sorting. `GET /campaigns/me` must return an empty list instead of raising `CampaignNotFoundException`.

## Acceptance criteria

- [ ] Single-campaign reads are owner-scoped.
- [ ] Missing or foreign-owned campaigns raise `CampaignNotFoundException`.
- [ ] List-my-campaigns returns a list and allows `[]`.

