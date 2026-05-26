# TASK-3 — Implement JWT Access Token Verifier

**Target file:** `src/main/java/com/lhn/client_fidelity/infrastructure/authentication/token/JwtAccessTokenVerifier.java` (new)
**SDD reference:** Section 3.2
**Depends on:** TASK-1, TASK-2
**Blocked by:** TASK-1, TASK-2

---

## Context

[JwtTokenService.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/infrastructure/authentication/token/JwtTokenService.java:27) defines the JWT format, claims, and HMAC-SHA256 signature generation, but there is no verifier for incoming bearer tokens. Campaign routes need a sibling component that validates header format, signature, claims, expiration, and user existence before any controller logic runs.

## What to do

Create `JwtAccessTokenVerifier` as the infrastructure implementation of `AccessTokenVerifier`.

Use the same signing inputs as token creation:

```java
public final class JwtAccessTokenVerifier implements AccessTokenVerifier {

    @Override
    public AuthenticatedUser verify(String authorizationHeader) {
        // 1. require Authorization header
        // 2. require Bearer scheme
        // 3. split into 3 JWT sections
        // 4. validate HMAC SHA-256 signature
        // 5. decode and validate sub, userType, exp
        // 6. load user by id and confirm current type matches token claim
    }
}
```

Implement these checks:

- missing or blank header -> `MissingAccessTokenException`
- non-`Bearer` scheme -> `InvalidAccessTokenException`
- token not in three sections -> `InvalidAccessTokenException`
- signature mismatch -> `InvalidAccessTokenException`
- missing `sub`, missing `userType`, or missing `exp` -> `InvalidAccessTokenException`
- expired token -> `InvalidAccessTokenException`
- unknown `userType` claim -> `InvalidAccessTokenException`
- `sub` not found in persistence -> `InvalidAccessTokenException`
- persisted user type different from token `userType` -> `InvalidAccessTokenException`

Return `new AuthenticatedUser(new UserId(sub), resolvedType)`.

## Implementation notes

Use `AuthenticationProperties` for `jwtSecret` and the injected `Clock` for expiration checks. Prefer decoding the payload with Jackson rather than ad hoc string parsing.

## Acceptance criteria

- [ ] The verifier uses the current JWT secret and HMAC-SHA256.
- [ ] The verifier validates `sub`, `userType`, and `exp`.
- [ ] The verifier rejects stale tokens whose subject user no longer exists.
- [ ] The verifier returns `AuthenticatedUser` for valid tokens.

