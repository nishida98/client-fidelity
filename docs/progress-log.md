# Progress Log

## 2026-05-26 - User Authentication Specification

### API Contracts

- Added consolidated API contracts document at `docs/api-contracts.md`.
- Documented current public contracts:
  - `POST /users`
  - `POST /auth/codes`
  - `POST /auth/token`

### Specification

- Added `docs/specs/user-authentication.md`.
- Defined a two-step authentication flow:
  - Request authentication code.
  - Verify code and receive JWT.
- Defined JWT as the application authentication mechanism.
- Defined authentication methods:
  - `EMAIL`
  - `PHONE`
- Defined email delivery implementations:
  - Local console provider.
  - SMTP provider.
  - Amazon SES provider.
- Defined phone delivery as a provider port/interface with the concrete provider still open.
- Chose database persistence for authentication codes instead of cache-only storage.
- Updated the authentication code storage rule to persist the generated code as plain text instead of hashing it.
- Defined 5-minute code expiration and 5-minute resend lockout while an active code exists.
- Added edge cases, expected errors, provider behavior, use case flow, and test coverage.
- Added implementation task breakdown under `docs/specs/user-authentication-tasks`.

### Implementation

- Implemented authentication domain model:
  - `AuthenticationMethod`
  - `AuthenticationChallenge`
  - `AuthenticationChallengeId`
  - `AuthenticationCode`
- Implemented authentication application ports and use cases:
  - `AuthenticationChallengeRepository`
  - `AuthenticationCodeDelivery`
  - `TokenService`
  - `RequestAuthenticationCodeUseCase`
  - `VerifyAuthenticationCodeUseCase`
- Extended `UserRepository` with lookup by normalized email and phone.
- Implemented authentication challenge persistence with JPA/H2.
- Implemented delivery providers:
  - Console email provider.
  - SMTP email provider, enabled only when configured.
  - SES provider placeholder, enabled only when configured.
  - Phone-not-configured provider.
- Implemented HMAC-SHA256 JWT token creation without adding a JWT library.
- Added REST endpoints:
  - `POST /auth/codes`
  - `POST /auth/token`
- Extended centralized REST error handling for authentication failures.
- Added tests for:
  - Auth domain behavior.
  - Request-code and verify-code use cases.
  - JPA authentication challenge persistence.
  - Console and phone delivery providers.
  - JWT token creation.
  - Auth API behavior.

### Verification

- Verified with:

```powershell
.\mvnw.cmd -q clean "-Djava.version=17" test
```

- The Java 17 override test run passes.

## 2026-05-25 - Create User Vertical Slice

### Specification

- Created and expanded `docs/specs/create-user.md`.
- Defined `POST /users` for two user types:
  - `COMMERCE`
  - `COMMERCE_CLIENT`
- Defined request and response contracts.
- Defined HTTP responses:
  - `201 Created`
  - `400 Bad Request`
  - `409 Conflict`
  - `422 Unprocessable Entity`
  - `500 Internal Server Error`
- Documented edge cases, exceptions, duplicate rules, and required tests.
- Added explicit validation rules for:
  - Email
  - Phone
  - Brazilian CNPJ

### Dependencies

- Added Spring validation support.
- Added Caelum Stella for CNPJ validation:
  - `br.com.caelum.stella:caelum-stella-core:2.2.2`
- Added H2 database support.
- Replaced JDBC persistence with ORM/JPA:
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-data-jpa-test`

### Configuration

- Replaced `application.properties` with `application.yml`.
- Current defaults:
  - Application name: `client-fidelity`
  - Unknown JSON fields fail validation.
  - JPA schema management uses `ddl-auto: update`.
  - H2 console is enabled at `/h2-console`.
  - Default persistence adapter is H2.
  - CSV persistence can be selected with `client-fidelity.persistence.type=csv`.

### Architecture

- Implemented a layered/hexagonal structure:
  - `domain`
  - `application`
  - `infrastructure`
  - `interfaces`
- Kept the use case dependent on a repository port instead of persistence details.
- Kept REST controllers thin.
- Kept persistence mapping inside infrastructure.

### Domain

- Added user model:
  - `User`
  - `Commerce`
  - `CommerceClient`
  - `UserType`
  - `UserId`
- Added value objects:
  - `Email`
  - `Phone`
  - `GovernmentIdentifier`
- Added Stella-backed `CnpjValidator`.
- Phone values are normalized to digits only.
- Email values are normalized to lowercase.
- CNPJ values are normalized to digits only.

### Application

- Added `CreateUserUseCase`.
- Added `CreateUserCommand`.
- Added `CreateUserResult`.
- Added `UserRepository` port.
- Added duplicate checks before insertion:
  - Commerce by normalized CNPJ.
  - Commerce client by normalized email.
- Added persistence race translation for duplicate insert failures.

### Exceptions

- Consolidated custom exceptions into `com.lhn.client_fidelity.exception`.
- Current custom exceptions:
  - `CommerceAlreadyExistsException`
  - `CommerceClientAlreadyExistsException`
  - `DuplicateUserPersistenceException`
  - `UnknownUserTypeException`
  - `UserCreationException`
  - `UserValidationException`

### REST API

- Added `UserController`.
- Added `CreateUserRequest`.
- Added `UserResponse`.
- Added centralized error mapping with `RestExceptionHandler`.
- Added API error response DTOs:
  - `ErrorResponse`
  - `ErrorDetailResponse`
- Strict request handling rejects unknown fields with `422 VALIDATION_FAILED`.
- Commerce client responses omit commerce-only fields.

### Persistence

- Added CSV repository adapter:
  - `CsvUserRepository`
  - `CsvUserRecord`
- Added ORM/H2 repository adapter:
  - `UserEntity`
  - `JpaUserCrudRepository`
  - `JpaUserMapper`
  - `JpaUserRepository`
- Removed the JDBC-based H2 repository.
- Removed hand-written `schema.sql`.
- H2 is the default persistence implementation.

### Tests

- Added domain tests:
  - `EmailTest`
  - `PhoneTest`
  - `GovernmentIdentifierTest`
- Added use case tests:
  - `CreateUserUseCaseTest`
- Added repository tests:
  - `CsvUserRepositoryTest`
  - `JpaUserRepositoryTest`
- Added REST API tests:
  - `UserControllerTest`

### Verification

- `.\mvnw.cmd test` is blocked in the current shell because the project is configured for Java 26 and the local Maven runtime uses Java 17.
- Verified with:

```powershell
.\mvnw.cmd -q "-Djava.version=17" test
```

- The Java 17 override test run passes.
