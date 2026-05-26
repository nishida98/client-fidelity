# TASK-15 — Add API And Persistence Tests

**Target file:** `src/test/java/com/lhn/client_fidelity/interfaces/rest/campaign/CampaignControllerTest.java` (new)
**SDD reference:** Sections 3.4, 3.5, and 6
**Depends on:** TASK-9, TASK-10, TASK-11, TASK-12, TASK-13, TASK-14
**Blocked by:** TASK-9, TASK-10, TASK-11, TASK-12, TASK-13, TASK-14

---

## Context

After the application and infrastructure pieces exist, the remaining risk is contract integration: bearer-token protection, HTTP status mapping, strict DTO parsing, and persistence behavior for both supported backends. The current codebase already verifies REST behavior with standalone MockMvc tests in [UserControllerTest.java](/C:/_development/client-fidelity/src/test/java/com/lhn/client_fidelity/interfaces/rest/user/UserControllerTest.java:1) and [AuthenticationControllerTest.java](/C:/_development/client-fidelity/src/test/java/com/lhn/client_fidelity/interfaces/rest/authentication/AuthenticationControllerTest.java:1).

## What to do

Create:

- `CampaignControllerTest`
- `JpaCampaignRepositoryTest`
- `CsvCampaignRepositoryTest`

API test coverage must include:

- `POST /campaigns` success
- `GET /campaigns/{id}` success
- `GET /campaigns/me` success and empty list
- `PUT /campaigns/{id}` success
- `PATCH /campaigns/{id}` success
- `DELETE /campaigns/{id}` success
- missing token -> `401`
- invalid token -> `401`
- `COMMERCE_CLIENT` token -> `403`
- foreign-owned or missing campaign -> `404`
- validation failures -> `422`

Persistence test coverage must include:

- H2 save and owner-scoped read
- H2 owner-scoped list
- H2 owner-scoped delete
- CSV save and owner-scoped read
- CSV owner-scoped list
- CSV owner-scoped delete
- template round-trip persistence

## Implementation notes

For controller tests, use the interceptor in the standalone MockMvc setup or inject a verifier double that exercises the same request attribute path as production. For persistence tests, keep them focused on repository behavior, not on controller-level validation.

## Acceptance criteria

- [ ] Controller tests cover the campaign HTTP statuses and shared error contract.
- [ ] H2 persistence tests verify owner-scoped save/read/list/delete behavior.
- [ ] CSV persistence tests verify owner-scoped save/read/list/delete behavior.

