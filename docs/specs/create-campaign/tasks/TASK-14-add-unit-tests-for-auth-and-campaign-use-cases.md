# TASK-14 — Add Unit Tests For Auth And Campaign Use Cases

**Target file:** `src/test/java/com/lhn/client_fidelity/infrastructure/authentication/token/JwtAccessTokenVerifierTest.java` (new)
**SDD reference:** Sections 3.2, 3.3, and 3.5
**Depends on:** TASK-3, TASK-6, TASK-7, TASK-8
**Blocked by:** TASK-3, TASK-6, TASK-7, TASK-8

---

## Context

The new behavior carries most of its risk in the auth verifier and the campaign application layer. The existing project already tests domain and use-case behavior directly, for example in [CreateUserUseCaseTest.java](/C:/_development/client-fidelity/src/test/java/com/lhn/client_fidelity/application/user/CreateUserUseCaseTest.java:1), so the campaign feature should follow the same test pyramid.

## What to do

Create focused unit tests for:

- `JwtAccessTokenVerifierTest`
- `CreateCampaignUseCaseTest`
- `GetCampaignByIdUseCaseTest`
- `ListCommerceCampaignsUseCaseTest`
- `ReplaceCampaignUseCaseTest`
- `PatchCampaignUseCaseTest`
- `DeleteCampaignUseCaseTest`

Cover the edge cases from the SDD:

- missing bearer header
- malformed token
- invalid signature
- expired token
- stale user id
- non-`COMMERCE` owner
- `points = 0`
- negative points
- invalid date format
- `expirationDate < startDate`
- non-object template
- patch with no fields
- owner-scoped not found
- successful hard delete

## Implementation notes

Keep these tests isolated with fake repositories and a fixed `Clock`. Do not depend on MockMvc or Spring context here; those belong in the API test task.

## Acceptance criteria

- [ ] Auth verifier unit tests cover valid and invalid token paths.
- [ ] Campaign use-case unit tests cover validation, owner scope, and mutation behavior.
- [ ] Tests are deterministic and use fixed timestamps.

