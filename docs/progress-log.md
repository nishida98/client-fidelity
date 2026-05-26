# Progress Log

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
