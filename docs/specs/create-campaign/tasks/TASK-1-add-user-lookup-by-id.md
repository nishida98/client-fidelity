# TASK-1 — Add User Lookup By Id

**Target file:** `src/main/java/com/lhn/client_fidelity/application/user/UserRepository.java` (existing)
**SDD reference:** Section 3.2
**Depends on:** none
**Blocked by:** none

---

## Context

`UserRepository` currently exposes lookup by email and phone only in [UserRepository.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/application/user/UserRepository.java:7). JWTs already carry the authenticated user id in `sub` in [JwtTokenService.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/infrastructure/authentication/token/JwtTokenService.java:32), so the campaign auth flow needs an id-based lookup to validate token subjects against persisted users.

## What to do

Extend the user repository contract and keep every existing implementation and fake in sync.

Update the port:

```java
public interface UserRepository {

    boolean existsCommerceByGovernmentIdentifier(String governmentIdentifier);

    boolean existsCommerceClientByEmail(String email);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    User save(User user);
}
```

Apply the same method to:

- `JpaUserCrudRepository`
- `JpaUserRepository`
- `CsvUserRepository`
- fake repositories inside `UserControllerTest`
- fake repositories inside `AuthenticationControllerTest`

For JPA, use the existing entity id. For CSV, scan records by id before mapping back to the domain object.

## Implementation notes

Do not change current lookup semantics for email or phone. This task is only about adding `findById` and keeping the project compiling after the port expansion.

## Acceptance criteria

- [ ] `UserRepository` exposes `findById(UserId id)`.
- [ ] H2 and CSV user repositories implement the new method.
- [ ] Existing controller test fakes compile against the expanded interface.
- [ ] Current user and authentication behavior is unchanged.

