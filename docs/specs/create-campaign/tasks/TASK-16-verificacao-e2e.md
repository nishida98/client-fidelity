# TASK-16 — E2E Verification

**Target file:** `docs/specs/create-campaign/SDD.md` (existing)
**SDD reference:** Section 6
**Depends on:** TASK-1, TASK-2, TASK-3, TASK-4, TASK-5, TASK-6, TASK-7, TASK-8, TASK-9, TASK-10, TASK-11, TASK-12, TASK-13, TASK-14, TASK-15
**Blocked by:** TASK-1, TASK-2, TASK-3, TASK-4, TASK-5, TASK-6, TASK-7, TASK-8, TASK-9, TASK-10, TASK-11, TASK-12, TASK-13, TASK-14, TASK-15

---

## Context

The SDD adds a full authenticated CRUD surface plus dual persistence support. The final task is to verify the complete feature against the acceptance criteria, not just that the individual files compile in isolation.

## What to do

Run the final verification checklist against the implemented code:

- [ ] `POST /campaigns`, `GET /campaigns/{id}`, `GET /campaigns/me`, `PUT /campaigns/{id}`, `PATCH /campaigns/{id}`, and `DELETE /campaigns/{id}` all require `Authorization: Bearer <jwt>`.
- [ ] A valid `COMMERCE` token can create, read, list, replace, patch, and delete its own campaigns.
- [ ] A valid `COMMERCE_CLIENT` token receives `403 FORBIDDEN_USER_TYPE`.
- [ ] Missing or malformed tokens return `401` with the expected error code.
- [ ] Missing or foreign-owned campaigns return `404 CAMPAIGN_NOT_FOUND`.
- [ ] Validation failures return `422 VALIDATION_FAILED` with field details when applicable.
- [ ] `points = 0` is accepted.
- [ ] `expirationDate < startDate` is rejected.
- [ ] Non-object templates are rejected.
- [ ] `GET /campaigns/me` returns `[]` for a commerce with no campaigns.
- [ ] H2 persistence mode works.
- [ ] CSV persistence mode works.

Run the full test suite:

```powershell
./mvnw test
```

## Implementation notes

If any acceptance criterion fails, fix the implementation task that owns the behavior instead of patching the verification task. Record any residual limitation only if it is explicitly accepted and reflected back into the SDD.

## Acceptance criteria

- [ ] Every SDD acceptance criterion is verified against the final implementation.
- [ ] `./mvnw test` passes without errors.
- [ ] No undocumented deviation remains between the SDD and the code.

