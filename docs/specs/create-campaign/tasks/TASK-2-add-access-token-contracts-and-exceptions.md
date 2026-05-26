# TASK-2 — Add Access Token Contracts And Exceptions

**Target file:** `src/main/java/com/lhn/client_fidelity/application/authentication/AccessTokenVerifier.java` (new)
**SDD reference:** Section 3.2
**Depends on:** TASK-1
**Blocked by:** TASK-1

---

## Context

The current authentication flow stops at token issuance in [AuthenticationController.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/interfaces/rest/authentication/AuthenticationController.java:13). Before adding a JWT gate for campaign routes, the codebase needs a small application contract for verified identities plus explicit exception types for missing, invalid, and unauthorized access tokens.

## What to do

Create the application-level auth contract and the access-token exception set.

Add the authenticated principal model:

```java
public record AuthenticatedUser(
        UserId userId,
        UserType userType
) {
}
```

Add the verifier port:

```java
public interface AccessTokenVerifier {

    AuthenticatedUser verify(String authorizationHeader);
}
```

Add these exceptions under `com.lhn.client_fidelity.exception`:

- `MissingAccessTokenException`
- `InvalidAccessTokenException`
- `ForbiddenUserTypeException`

Keep the exception messages aligned with the SDD so the REST error handler can map them without inventing copy later.

## Implementation notes

These classes must stay framework-free. `ForbiddenUserTypeException` is for a valid token with a non-`COMMERCE` user type; it is not a substitute for malformed or expired token failures.

## Acceptance criteria

- [ ] `AuthenticatedUser` and `AccessTokenVerifier` exist in the authentication application package.
- [ ] Missing, invalid, and forbidden-token exceptions exist in the exception package.
- [ ] No REST wiring or cryptographic verification is implemented yet.

