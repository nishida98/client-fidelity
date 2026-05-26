# TASK-12 — Add Campaign Controller And Response DTO

**Target file:** `src/main/java/com/lhn/client_fidelity/interfaces/rest/campaign/CampaignController.java` (new)
**SDD reference:** Sections 3.1 and 4
**Depends on:** TASK-4, TASK-6, TASK-7, TASK-8, TASK-11
**Blocked by:** TASK-4, TASK-6, TASK-7, TASK-8, TASK-11

---

## Context

The current REST layer has patterns for created responses in [UserController.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/interfaces/rest/user/UserController.java:23) and simple authentication endpoints in [AuthenticationController.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/interfaces/rest/authentication/AuthenticationController.java:28). The new campaign controller should follow those conventions while reading the authenticated commerce from the interceptor request attribute.

## What to do

Create:

- `CampaignResponse`
- `CampaignController`

Expose these endpoints:

```java
@RestController
@RequestMapping("/campaigns")
public class CampaignController {

    @PostMapping
    public ResponseEntity<CampaignResponse> create(@RequestBody CreateCampaignRequest request, HttpServletRequest httpRequest) { }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getById(@PathVariable String id, HttpServletRequest httpRequest) { }

    @GetMapping("/me")
    public ResponseEntity<List<CampaignResponse>> listMine(HttpServletRequest httpRequest) { }

    @PutMapping("/{id}")
    public ResponseEntity<CampaignResponse> replace(@PathVariable String id, @RequestBody ReplaceCampaignRequest request, HttpServletRequest httpRequest) { }

    @PatchMapping("/{id}")
    public ResponseEntity<CampaignResponse> patch(@PathVariable String id, @RequestBody PatchCampaignRequest request, HttpServletRequest httpRequest) { }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, HttpServletRequest httpRequest) { }
}
```

Read the principal from `JwtAuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE` and pass its `userId` to each use case.

## Implementation notes

Use `201 Created` plus `Location` for create, `200 OK` for reads and updates, and `204 No Content` for delete. Keep the controller thin; all business validation stays in the use cases.

## Acceptance criteria

- [ ] All six campaign routes exist under `/campaigns`.
- [ ] The controller reads the authenticated commerce id from the interceptor attribute.
- [ ] Response bodies match the SDD contract.

