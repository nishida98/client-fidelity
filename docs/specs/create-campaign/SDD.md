# SDD — Create Campaign

## 1. Context and Problem

The current API exposes only unauthenticated user creation and authentication endpoints. `POST /users` is open in [UserController.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/interfaces/rest/user/UserController.java:13), and the authentication routes in [AuthenticationController.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/interfaces/rest/authentication/AuthenticationController.java:13) only issue JWTs; there is no route today that consumes a JWT to authorize business actions. The public contract summary in [api-contracts.md](/C:/_development/client-fidelity/docs/api-contracts.md:31) also has no campaign route.

JWT generation already includes the authenticated user id in the `sub` claim and the user type in the `userType` claim in [JwtTokenService.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/infrastructure/authentication/token/JwtTokenService.java:27). That means the authenticated commerce id can be derived from the token, but there is no verifier, interceptor, or protected controller flow yet. Unknown JSON fields are currently rejected globally through Jackson configuration in [application.yml](/C:/_development/client-fidelity/src/main/resources/application.yml:4) and mapped to `VALIDATION_FAILED` in [RestExceptionHandler.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/interfaces/rest/error/RestExceptionHandler.java:35).

The feature required here is an authenticated campaign management API for commerce users. It must allow only users of type `COMMERCE` to create, read, list their own campaigns, update, partially update, and delete campaigns. Each campaign must have an id, name, points, start date, expiration date, updated date, and a structured card template payload. The template must be accepted as an opaque JSON object and stored without schema-specific validation. The routes must return clear success and failure responses for authentication failures, authorization failures, ownership violations, validation failures, and persistence failures.

---

## 2. Scope of the Fix

### 2.1 What changes

| Area | Current situation | Target situation |
|---|---|---|
| Authentication consumption | JWTs are created but not verified by any REST route. | All `/campaigns` routes require `Authorization: Bearer <jwt>` and validate signature, expiration, `sub`, and `userType`. |
| Authorization | No route differentiates `COMMERCE` from `COMMERCE_CLIENT`. | Only authenticated `COMMERCE` users can manage their own campaigns. `COMMERCE_CLIENT` tokens receive `403 Forbidden`. |
| Campaign domain | No campaign model, repository, or use case exists. | Add campaign domain, application use case, repository port, and persistence adapters. |
| Campaign REST contract | No `/campaigns` endpoint exists. | Add `POST /campaigns`, `GET /campaigns/{id}`, `GET /campaigns/me`, `DELETE /campaigns/{id}`, `PUT /campaigns/{id}`, and `PATCH /campaigns/{id}` with strict top-level request validation, opaque template object support, and stable error responses. |
| Persistence | Existing repositories only cover users and authentication challenges. | Add campaign persistence for both configured persistence modes: H2 and CSV, preserving the project convention in [application.yml](/C:/_development/client-fidelity/src/main/resources/application.yml:15). |
| Error handling | Error mappings cover user creation and authentication flows only. | Add mappings for access-token errors, forbidden user type, campaign validation, campaign-not-found, and campaign operation failures. |

### 2.2 What does not change

- No template schema or rendering engine is introduced. The template payload is only required to be a JSON object.
- No change is made to JWT issuance semantics in `/auth/token`; the route continues to issue tokens as today.
- No global adoption of Spring Security is introduced for this feature.
- No campaign uniqueness rule is introduced beyond generating a unique campaign id.
- No time-based business rule is added that forbids creating campaigns with a past `startDate` or past `expirationDate`; only date consistency is enforced.
- No pagination, filtering, or sorting is added to `GET /campaigns/me` in this first version.
- No admin or cross-commerce campaign access is introduced.
- Delete is a hard delete; no soft-delete state, restore flow, or audit endpoint is introduced.

---

## 3. Solution Design

### 3.1 Route contract and validation

Add campaign routes in a new REST controller under `interfaces.rest.campaign`. `commerceId` must not be accepted in any request body because ownership comes from the JWT `sub` claim already present in [JwtTokenService.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/infrastructure/authentication/token/JwtTokenService.java:32). All campaign routes are scoped to the authenticated commerce user:

- `POST /campaigns`
- `GET /campaigns/{id}`
- `GET /campaigns/me`
- `DELETE /campaigns/{id}`
- `PUT /campaigns/{id}`
- `PATCH /campaigns/{id}`

If a campaign does not exist or does not belong to the authenticated commerce, the API returns `404 Not Found`. This avoids leaking campaign existence across commerces.

`POST /campaigns` request body must contain `name`, `points`, `startDate`, `expirationDate`, and `template`. The response returns the created campaign with server-generated `id` and `updatedAt`.

Request contract:

```http
POST /campaigns
Authorization: Bearer <jwt>
Content-Type: application/json
Accept: application/json
```

```json
{
  "name": "Campanha de Inverno",
  "points": 0,
  "startDate": "2026-06-01",
  "expirationDate": "2026-06-30",
  "template": {
    "backgroundColor": "#0F172A",
    "title": "Ganhe pontos em dobro",
    "ctaLabel": "Participar"
  }
}
```

Response contract:

```http
201 Created
Location: /campaigns/{campaignId}
```

```json
{
  "id": "cam_01J0Z7V3Y2Y9PQ8N5X4M3K2H1F",
  "name": "Campanha de Inverno",
  "points": 0,
  "startDate": "2026-06-01",
  "expirationDate": "2026-06-30",
  "updatedAt": "2026-05-26T13:00:00Z",
  "template": {
    "backgroundColor": "#0F172A",
    "title": "Ganhe pontos em dobro",
    "ctaLabel": "Participar"
  }
}
```

Validation rules:

- `name`: required, trimmed, must not be blank.
- `points`: required, integer, must be `>= 0`.
- `startDate`: required, ISO-8601 date string (`yyyy-MM-dd`).
- `expirationDate`: required, ISO-8601 date string (`yyyy-MM-dd`), must be equal to or after `startDate`.
- `template`: required, must be a JSON object; arrays, strings, booleans, numbers, and `null` are rejected.
- Unknown top-level request fields are rejected with `422 VALIDATION_FAILED`, matching the current strict request style in [CreateUserRequest.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/interfaces/rest/user/CreateUserRequest.java:12).
- Unknown nested fields inside `template` are allowed because the template is intentionally opaque.

Additional route contracts:

`GET /campaigns/{id}`

```http
200 OK
```

```json
{
  "id": "cam_01J0Z7V3Y2Y9PQ8N5X4M3K2H1F",
  "name": "Campanha de Inverno",
  "points": 0,
  "startDate": "2026-06-01",
  "expirationDate": "2026-06-30",
  "updatedAt": "2026-05-26T13:00:00Z",
  "template": {
    "backgroundColor": "#0F172A",
    "title": "Ganhe pontos em dobro",
    "ctaLabel": "Participar"
  }
}
```

`GET /campaigns/me`

```http
200 OK
```

```json
[
  {
    "id": "cam_01J0Z7V3Y2Y9PQ8N5X4M3K2H1F",
    "name": "Campanha de Inverno",
    "points": 0,
    "startDate": "2026-06-01",
    "expirationDate": "2026-06-30",
    "updatedAt": "2026-05-26T13:00:00Z",
    "template": {
      "backgroundColor": "#0F172A"
    }
  },
  {
    "id": "cam_01J0Z82BX6B9P4M6H7K1N2R3S4",
    "name": "Campanha de Verao",
    "points": 25,
    "startDate": "2026-07-01",
    "expirationDate": "2026-07-31",
    "updatedAt": "2026-05-26T13:05:00Z",
    "template": {
      "backgroundColor": "#F59E0B"
    }
  }
]
```

If the authenticated commerce has no campaigns, return:

```json
[]
```

`DELETE /campaigns/{id}`

```http
204 No Content
```

`PUT /campaigns/{id}` uses the same body contract and validation rules as `POST /campaigns`, except `id` remains immutable and `updatedAt` is overwritten server-side.

`PATCH /campaigns/{id}` accepts a partial JSON object with any subset of:

- `name`
- `points`
- `startDate`
- `expirationDate`
- `template`

Patch rules:

- At least one recognized field must be present.
- Unknown top-level fields are rejected with `422 VALIDATION_FAILED`.
- Provided fields must satisfy the same validation rules as `POST /campaigns`.
- Omitted fields keep their current persisted values.
- `null` is rejected for editable fields because this model has no nullable editable fields.
- If either `startDate` or `expirationDate` changes, the final combined state must still satisfy `expirationDate >= startDate`.

Recommended request DTO shape:

```java
package com.lhn.client_fidelity.interfaces.rest.campaign;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.lhn.client_fidelity.application.campaign.CreateCampaignCommand;
import com.lhn.client_fidelity.domain.user.FieldValidationError;
import com.lhn.client_fidelity.exception.CampaignValidationException;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public final class CreateCampaignRequest {

    private String name;
    private Integer points;
    private String startDate;
    private String expirationDate;
    private JsonNode template;
    private final List<String> unknownFields = new ArrayList<>();

    public void setName(String name) {
        this.name = name;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setTemplate(JsonNode template) {
        this.template = template;
    }

    @JsonAnySetter
    public void setUnknownField(String field, Object value) {
        unknownFields.add(field);
    }

    CreateCampaignCommand toCommand(String commerceId) {
        if (!unknownFields.isEmpty()) {
            throw new CampaignValidationException(unknownFields.stream()
                    .map(field -> new FieldValidationError(field, "Field is not allowed."))
                    .toList());
        }
        return new CreateCampaignCommand(
                commerceId,
                name,
                points,
                startDate,
                expirationDate,
                template
        );
    }
}
```

This keeps top-level validation behavior consistent with [RestExceptionHandler.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/interfaces/rest/error/RestExceptionHandler.java:61) while leaving the template object unconstrained internally.

For `PUT`, add a `ReplaceCampaignRequest` with the same structure as `CreateCampaignRequest`.

For `PATCH`, add a dedicated request DTO so field presence can be distinguished from absence:

```java
package com.lhn.client_fidelity.interfaces.rest.campaign;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.lhn.client_fidelity.application.campaign.PatchCampaignCommand;
import com.lhn.client_fidelity.domain.user.FieldValidationError;
import com.lhn.client_fidelity.exception.CampaignValidationException;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public final class PatchCampaignRequest {

    private String name;
    private boolean namePresent;
    private Integer points;
    private boolean pointsPresent;
    private String startDate;
    private boolean startDatePresent;
    private String expirationDate;
    private boolean expirationDatePresent;
    private JsonNode template;
    private boolean templatePresent;
    private final List<String> unknownFields = new ArrayList<>();

    public void setName(String name) {
        this.name = name;
        this.namePresent = true;
    }

    public void setPoints(Integer points) {
        this.points = points;
        this.pointsPresent = true;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
        this.startDatePresent = true;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
        this.expirationDatePresent = true;
    }

    public void setTemplate(JsonNode template) {
        this.template = template;
        this.templatePresent = true;
    }

    @JsonAnySetter
    public void setUnknownField(String field, Object value) {
        unknownFields.add(field);
    }

    PatchCampaignCommand toCommand(String campaignId, String commerceId) {
        if (!unknownFields.isEmpty()) {
            throw new CampaignValidationException(unknownFields.stream()
                    .map(field -> new FieldValidationError(field, "Field is not allowed."))
                    .toList());
        }
        return new PatchCampaignCommand(
                campaignId,
                commerceId,
                name,
                namePresent,
                points,
                pointsPresent,
                startDate,
                startDatePresent,
                expirationDate,
                expirationDatePresent,
                template,
                templatePresent
        );
    }
}
```

### 3.2 JWT verification and route protection

There is currently no verifier for the JWTs created in [JwtTokenService.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/infrastructure/authentication/token/JwtTokenService.java:27). Instead of adding Spring Security, use a focused MVC interceptor plus a verifier service:

- `AccessTokenVerifier` application port
- `JwtAccessTokenVerifier` infrastructure implementation
- `JwtAuthenticationInterceptor` registered only for `/campaigns/**`

The verifier must:

1. Read the `Authorization` header.
2. Require the `Bearer` scheme.
3. Split the JWT into three sections.
4. Recompute the HMAC SHA-256 signature using `client-fidelity.authentication.jwt-secret` from [AuthenticationProperties.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/application/authentication/AuthenticationProperties.java:18).
5. Decode and validate the claims:
   - `sub` must be present and non-blank.
   - `userType` must be present and map to an existing `UserType`.
   - `exp` must be present and be in the future.
6. Load the user by id to reject orphan or stale tokens. `UserRepository` does not currently support this lookup in [UserRepository.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/application/user/UserRepository.java:7), so add `Optional<User> findById(UserId id)` and implement it in both repositories.
7. Reject the token if the persisted user does not exist or its current type does not match the token claim.
8. Attach an `AuthenticatedUser` record to the request for controller access.

Recommended auth model:

```java
package com.lhn.client_fidelity.application.authentication;

import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.domain.user.UserType;

public record AuthenticatedUser(
        UserId userId,
        UserType userType
) {
}
```

```java
package com.lhn.client_fidelity.application.authentication;

public interface AccessTokenVerifier {

    AuthenticatedUser verify(String authorizationHeader);
}
```

Recommended interceptor usage:

```java
package com.lhn.client_fidelity.interfaces.rest.authentication;

import com.lhn.client_fidelity.application.authentication.AccessTokenVerifier;
import com.lhn.client_fidelity.application.authentication.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public final class JwtAuthenticationInterceptor implements HandlerInterceptor {

    public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser";

    private final AccessTokenVerifier accessTokenVerifier;

    public JwtAuthenticationInterceptor(AccessTokenVerifier accessTokenVerifier) {
        this.accessTokenVerifier = accessTokenVerifier;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        AuthenticatedUser authenticatedUser =
                accessTokenVerifier.verify(request.getHeader("Authorization"));
        request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, authenticatedUser);
        return true;
    }
}
```

Authentication and authorization responses for this route:

| Status | Code | When |
|---|---|---|
| `401 Unauthorized` | `MISSING_ACCESS_TOKEN` | `Authorization` header is missing or blank. |
| `401 Unauthorized` | `INVALID_ACCESS_TOKEN` | Header is not `Bearer`, token is malformed, signature is invalid, token is expired, claims are missing, or the token subject is not a valid current user. |
| `403 Forbidden` | `FORBIDDEN_USER_TYPE` | Token is valid, but `userType` is not `COMMERCE`. |

This keeps the route behavior explicit without retrofitting full security infrastructure into the whole application.

### 3.3 Campaign domain and use case

Introduce a small campaign domain model with no framework dependencies. `updatedAt` is server-managed and set to the current clock instant on creation and update paths. On the create path, `updatedAt` equals the creation time; there is no separate `createdAt` field in this contract because it was not requested.

Recommended domain model:

```java
package com.lhn.client_fidelity.domain.campaign;

import com.fasterxml.jackson.databind.JsonNode;
import com.lhn.client_fidelity.domain.user.UserId;

import java.time.Instant;
import java.time.LocalDate;

public record Campaign(
        CampaignId id,
        UserId commerceId,
        String name,
        int points,
        LocalDate startDate,
        LocalDate expirationDate,
        Instant updatedAt,
        JsonNode template
) {
}
```

Recommended use case:

```java
package com.lhn.client_fidelity.application.campaign;

import com.fasterxml.jackson.databind.JsonNode;
import com.lhn.client_fidelity.application.user.UserRepository;
import com.lhn.client_fidelity.domain.campaign.Campaign;
import com.lhn.client_fidelity.domain.campaign.CampaignId;
import com.lhn.client_fidelity.domain.user.User;
import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.domain.user.UserType;
import com.lhn.client_fidelity.exception.CampaignCreationException;
import com.lhn.client_fidelity.exception.CampaignValidationException;
import com.lhn.client_fidelity.exception.ForbiddenUserTypeException;
import com.lhn.client_fidelity.exception.InvalidAccessTokenException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

public final class CreateCampaignUseCase {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public CreateCampaignUseCase(
            CampaignRepository campaignRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.campaignRepository = campaignRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    public CreateCampaignResult execute(CreateCampaignCommand command) {
        User owner = userRepository.findById(new UserId(command.commerceId()))
                .orElseThrow(() -> new InvalidAccessTokenException("Access token is invalid."));

        if (owner.type() != UserType.COMMERCE) {
            throw new ForbiddenUserTypeException("Only commerce users can create campaigns.");
        }

        String normalizedName = requireName(command.name());
        int normalizedPoints = requirePoints(command.points());
        LocalDate startDate = requireDate("startDate", command.startDate());
        LocalDate expirationDate = requireDate("expirationDate", command.expirationDate());
        JsonNode template = requireTemplate(command.template());

        if (expirationDate.isBefore(startDate)) {
            throw CampaignValidationException.single(
                    "expirationDate",
                    "Expiration date must be equal to or after start date."
            );
        }

        Instant now = clock.instant();
        Campaign campaign = new Campaign(
                CampaignId.newId(),
                owner.id(),
                normalizedName,
                normalizedPoints,
                startDate,
                expirationDate,
                now,
                template
        );

        try {
            Campaign savedCampaign = campaignRepository.save(campaign);
            return CreateCampaignResult.from(savedCampaign);
        }
        catch (RuntimeException exception) {
            throw new CampaignCreationException(exception);
        }
    }

    // helper methods unchanged
}
```

Key behavioral decisions:

- `points = 0` is valid.
- `expirationDate == startDate` is valid.
- `template = {}` is valid because the object is opaque.
- Duplicate campaign names are allowed because no uniqueness rule was requested.
- The controller must not accept `updatedAt` from clients.
- `GET /campaigns/me` returns all campaigns owned by the authenticated commerce with no pagination in this first version.
- `DELETE /campaigns/{id}` is a hard delete and returns `204 No Content` when successful.
- `PUT /campaigns/{id}` fully replaces editable fields and updates `updatedAt`.
- `PATCH /campaigns/{id}` partially updates editable fields and updates `updatedAt`.
- `GET`, `PUT`, `PATCH`, and `DELETE` use owner-scoped lookup; a campaign owned by another commerce is treated as not found.

Add the following application use cases:

- `GetCampaignByIdUseCase`
- `ListCommerceCampaignsUseCase`
- `ReplaceCampaignUseCase`
- `PatchCampaignUseCase`
- `DeleteCampaignUseCase`

Repository contract should support owner-scoped operations to keep authorization checks at the application boundary without loading unrelated campaigns:

```java
package com.lhn.client_fidelity.application.campaign;

import com.lhn.client_fidelity.domain.campaign.Campaign;
import com.lhn.client_fidelity.domain.campaign.CampaignId;
import com.lhn.client_fidelity.domain.user.UserId;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository {

    Campaign save(Campaign campaign);

    Optional<Campaign> findByIdAndCommerceId(CampaignId campaignId, UserId commerceId);

    List<Campaign> findAllByCommerceId(UserId commerceId);

    void deleteByIdAndCommerceId(CampaignId campaignId, UserId commerceId);
}
```

`deleteByIdAndCommerceId` should report whether a row or record was removed. If nothing is removed, the application must translate that to `CampaignNotFoundException`.

### 3.4 Persistence strategy

The project already supports two persistence modes through `client-fidelity.persistence.type` in [application.yml](/C:/_development/client-fidelity/src/main/resources/application.yml:15), with H2 and CSV implementations for users in [JpaUserRepository.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/infrastructure/user/h2/JpaUserRepository.java:14) and [CsvUserRepository.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/infrastructure/user/csv/CsvUserRepository.java:20). Campaign persistence should follow the same convention so the new feature does not silently depend on only one environment.

H2 design:

- Create `campaigns` table with:
  - `id`
  - `commerce_id`
  - `name`
  - `points`
  - `start_date`
  - `expiration_date`
  - `updated_at`
  - `template_payload`
- Store `template_payload` as a JSON string in a large text column.
- Add an index on `commerce_id`.
- No unique constraint on `name`.
- Support owner-scoped lookup queries by `id` and `commerce_id`.
- Support owner-scoped delete by `id` and `commerce_id`.

CSV design:

- Create `data/campaigns.csv` beside the existing user CSV file convention.
- Persist one line per campaign with the same fields as the H2 entity.
- Serialize `template` to compact JSON text.
- Filter records in memory for `findAllByCommerceId`, `findByIdAndCommerceId`, and delete/replace/patch flows.

Recommended repository contract:

```java
package com.lhn.client_fidelity.application.campaign;

import com.lhn.client_fidelity.domain.campaign.Campaign;
import com.lhn.client_fidelity.domain.campaign.CampaignId;
import com.lhn.client_fidelity.domain.user.UserId;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository {

    Campaign save(Campaign campaign);

    Optional<Campaign> findByIdAndCommerceId(CampaignId campaignId, UserId commerceId);

    List<Campaign> findAllByCommerceId(UserId commerceId);

    void deleteByIdAndCommerceId(CampaignId campaignId, UserId commerceId);
}
```

The persistence adapters should translate create failures into `CampaignCreationException`, update failures into `CampaignUpdateException`, and delete failures into `CampaignDeletionException`, following the same wrapping pattern used for users in [JpaUserRepository.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/infrastructure/user/h2/JpaUserRepository.java:45).

### 3.5 REST error mapping

Extend [RestExceptionHandler.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/interfaces/rest/error/RestExceptionHandler.java:26) to map the new exception set while preserving the existing shared error shape:

| Exception | HTTP status | Error code | Message |
|---|---|---|---|
| `MissingAccessTokenException` | `401 Unauthorized` | `MISSING_ACCESS_TOKEN` | `Authorization header with Bearer token is required.` |
| `InvalidAccessTokenException` | `401 Unauthorized` | `INVALID_ACCESS_TOKEN` | `Access token is invalid or expired.` |
| `ForbiddenUserTypeException` | `403 Forbidden` | `FORBIDDEN_USER_TYPE` | `Only commerce users can create campaigns.` |
| `CampaignNotFoundException` | `404 Not Found` | `CAMPAIGN_NOT_FOUND` | `Campaign was not found.` |
| `CampaignValidationException` | `422 Unprocessable Entity` | `VALIDATION_FAILED` | `The request contains invalid fields.` |
| `CampaignCreationException` | `500 Internal Server Error` | `CAMPAIGN_CREATION_FAILED` | `Campaign could not be created.` |
| `CampaignUpdateException` | `500 Internal Server Error` | `CAMPAIGN_UPDATE_FAILED` | `Campaign could not be updated.` |
| `CampaignDeletionException` | `500 Internal Server Error` | `CAMPAIGN_DELETION_FAILED` | `Campaign could not be deleted.` |

Edge cases the handler and tests must cover:

- Missing `Authorization` header.
- Blank `Authorization` header.
- Non-Bearer header such as `Basic abc`.
- Bearer token with fewer or more than three JWT sections.
- Signature mismatch.
- Expired token.
- Missing `sub`.
- Missing `userType`.
- Unknown `userType`.
- Token whose `sub` no longer resolves to a persisted user.
- Valid token for `COMMERCE_CLIENT`.
- Valid token with invalid body.
- Valid token with unknown top-level fields.
- Valid token with `template` not being an object.
- Valid token requesting another commerce's campaign id.
- `PATCH` with empty object.
- `PATCH` with all fields `null`.
- `DELETE` for an already deleted campaign.

---

## 4. Flow after the fix

```text
Client
  |
  | POST /campaigns
  | GET /campaigns/{id}
  | GET /campaigns/me
  | PUT /campaigns/{id}
  | PATCH /campaigns/{id}
  | DELETE /campaigns/{id}
  | Authorization: Bearer <jwt>
  | body { ... } when applicable
  v
JwtAuthenticationInterceptor
  |
  |-- verify header format
  |-- verify JWT signature and exp
  |-- read sub -> commerce user id
  |-- read userType
  |-- load user by id
  v
CampaignController
  |
  |-- reject if authenticated user type != COMMERCE
  |-- resolve authenticated commerce id from request attribute
  |-- route to create/get/list/replace/patch/delete use case
  v
Campaign application use case
  |
  |-- validate owner scope
  |-- validate request fields when applicable
  |-- load campaign by id and commerce id when applicable
  |-- set updatedAt from Clock on create/update paths
  |-- persist or delete
  v
CampaignRepository
  |
  |-- H2 adapter when persistence.type=h2
  |-- CSV adapter when persistence.type=csv
  v
Persisted campaign
  |
  v
201/200/204 response
```

---

## 5. Files to modify/create

| File | Type of change |
|---|---|
| `src/main/java/com/lhn/client_fidelity/application/user/UserRepository.java` | Modify — add `findById(UserId id)` for authenticated owner lookup. |
| `src/main/java/com/lhn/client_fidelity/infrastructure/user/h2/JpaUserCrudRepository.java` | Modify — add `findById` support for user lookup by JWT subject. |
| `src/main/java/com/lhn/client_fidelity/infrastructure/user/h2/JpaUserRepository.java` | Modify — implement `findById`. |
| `src/main/java/com/lhn/client_fidelity/infrastructure/user/csv/CsvUserRepository.java` | Modify — implement `findById`. |
| `src/main/java/com/lhn/client_fidelity/interfaces/rest/error/RestExceptionHandler.java` | Modify — add access-token and campaign exception mappings. |
| `src/test/java/com/lhn/client_fidelity/interfaces/rest/user/UserControllerTest.java` | Modify — adapt fake repository to the expanded `UserRepository` contract. |
| `src/test/java/com/lhn/client_fidelity/interfaces/rest/authentication/AuthenticationControllerTest.java` | Modify — adapt fake repository to the expanded `UserRepository` contract. |
| `src/main/java/com/lhn/client_fidelity/application/authentication/AuthenticatedUser.java` | **Create** — authenticated principal model derived from JWT. |
| `src/main/java/com/lhn/client_fidelity/application/authentication/AccessTokenVerifier.java` | **Create** — application port for JWT verification. |
| `src/main/java/com/lhn/client_fidelity/infrastructure/authentication/token/JwtAccessTokenVerifier.java` | **Create** — verifies signature, claims, expiration, and user existence. |
| `src/main/java/com/lhn/client_fidelity/interfaces/rest/authentication/JwtAuthenticationInterceptor.java` | **Create** — protects `/campaigns/**` and stores authenticated user in the request. |
| `src/main/java/com/lhn/client_fidelity/interfaces/rest/authentication/WebMvcAuthenticationConfiguration.java` | **Create** — registers the interceptor only for campaign routes. |
| `src/main/java/com/lhn/client_fidelity/domain/campaign/Campaign.java` | **Create** — campaign aggregate. |
| `src/main/java/com/lhn/client_fidelity/domain/campaign/CampaignId.java` | **Create** — campaign id value object. |
| `src/main/java/com/lhn/client_fidelity/application/campaign/CreateCampaignCommand.java` | **Create** — request command for create flow. |
| `src/main/java/com/lhn/client_fidelity/application/campaign/CreateCampaignResult.java` | **Create** — response model for REST mapping. |
| `src/main/java/com/lhn/client_fidelity/application/campaign/CreateCampaignUseCase.java` | **Create** — validates and persists new campaigns. |
| `src/main/java/com/lhn/client_fidelity/application/campaign/GetCampaignByIdUseCase.java` | **Create** — owner-scoped single-campaign lookup. |
| `src/main/java/com/lhn/client_fidelity/application/campaign/ListCommerceCampaignsUseCase.java` | **Create** — owner-scoped campaign listing. |
| `src/main/java/com/lhn/client_fidelity/application/campaign/ReplaceCampaignCommand.java` | **Create** — command for full replacement. |
| `src/main/java/com/lhn/client_fidelity/application/campaign/ReplaceCampaignUseCase.java` | **Create** — full update flow. |
| `src/main/java/com/lhn/client_fidelity/application/campaign/PatchCampaignCommand.java` | **Create** — command for partial update. |
| `src/main/java/com/lhn/client_fidelity/application/campaign/PatchCampaignUseCase.java` | **Create** — partial update flow. |
| `src/main/java/com/lhn/client_fidelity/application/campaign/DeleteCampaignUseCase.java` | **Create** — hard delete flow. |
| `src/main/java/com/lhn/client_fidelity/application/campaign/CampaignRepository.java` | **Create** — persistence port. |
| `src/main/java/com/lhn/client_fidelity/interfaces/rest/campaign/CreateCampaignRequest.java` | **Create** — strict request DTO with opaque template support. |
| `src/main/java/com/lhn/client_fidelity/interfaces/rest/campaign/ReplaceCampaignRequest.java` | **Create** — strict full-update DTO. |
| `src/main/java/com/lhn/client_fidelity/interfaces/rest/campaign/PatchCampaignRequest.java` | **Create** — presence-aware partial-update DTO. |
| `src/main/java/com/lhn/client_fidelity/interfaces/rest/campaign/CampaignResponse.java` | **Create** — REST response DTO. |
| `src/main/java/com/lhn/client_fidelity/interfaces/rest/campaign/CampaignController.java` | **Create** — campaign create/read/list/update/delete endpoints. |
| `src/main/java/com/lhn/client_fidelity/exception/MissingAccessTokenException.java` | **Create** — 401 for missing bearer header. |
| `src/main/java/com/lhn/client_fidelity/exception/InvalidAccessTokenException.java` | **Create** — 401 for malformed, invalid, or expired token. |
| `src/main/java/com/lhn/client_fidelity/exception/ForbiddenUserTypeException.java` | **Create** — 403 when non-commerce users call the route. |
| `src/main/java/com/lhn/client_fidelity/exception/CampaignNotFoundException.java` | **Create** — 404 when the campaign does not exist in the authenticated owner scope. |
| `src/main/java/com/lhn/client_fidelity/exception/CampaignValidationException.java` | **Create** — 422 field validation error container. |
| `src/main/java/com/lhn/client_fidelity/exception/CampaignCreationException.java` | **Create** — 500 wrapper for persistence or unexpected create failures. |
| `src/main/java/com/lhn/client_fidelity/exception/CampaignUpdateException.java` | **Create** — 500 wrapper for replace/patch failures. |
| `src/main/java/com/lhn/client_fidelity/exception/CampaignDeletionException.java` | **Create** — 500 wrapper for delete failures. |
| `src/main/java/com/lhn/client_fidelity/infrastructure/campaign/h2/CampaignEntity.java` | **Create** — JPA entity for campaigns. |
| `src/main/java/com/lhn/client_fidelity/infrastructure/campaign/h2/JpaCampaignCrudRepository.java` | **Create** — Spring Data JPA repository for campaigns. |
| `src/main/java/com/lhn/client_fidelity/infrastructure/campaign/h2/JpaCampaignRepository.java` | **Create** — H2 adapter for `CampaignRepository`. |
| `src/main/java/com/lhn/client_fidelity/infrastructure/campaign/csv/CsvCampaignRecord.java` | **Create** — CSV serialization/deserialization helper. |
| `src/main/java/com/lhn/client_fidelity/infrastructure/campaign/csv/CsvCampaignRepository.java` | **Create** — CSV adapter for `CampaignRepository`. |
| `src/test/java/com/lhn/client_fidelity/application/campaign/CreateCampaignUseCaseTest.java` | **Create** — unit coverage for validation and ownership rules. |
| `src/test/java/com/lhn/client_fidelity/application/campaign/GetCampaignByIdUseCaseTest.java` | **Create** — unit coverage for owner-scoped lookup. |
| `src/test/java/com/lhn/client_fidelity/application/campaign/ListCommerceCampaignsUseCaseTest.java` | **Create** — unit coverage for owner-scoped listing. |
| `src/test/java/com/lhn/client_fidelity/application/campaign/ReplaceCampaignUseCaseTest.java` | **Create** — unit coverage for full update validation and ownership rules. |
| `src/test/java/com/lhn/client_fidelity/application/campaign/PatchCampaignUseCaseTest.java` | **Create** — unit coverage for partial update validation and ownership rules. |
| `src/test/java/com/lhn/client_fidelity/application/campaign/DeleteCampaignUseCaseTest.java` | **Create** — unit coverage for hard delete behavior. |
| `src/test/java/com/lhn/client_fidelity/infrastructure/authentication/token/JwtAccessTokenVerifierTest.java` | **Create** — unit coverage for token verification edge cases. |
| `src/test/java/com/lhn/client_fidelity/infrastructure/campaign/h2/JpaCampaignRepositoryTest.java` | **Create** — integration coverage for H2 persistence. |
| `src/test/java/com/lhn/client_fidelity/infrastructure/campaign/csv/CsvCampaignRepositoryTest.java` | **Create** — integration coverage for CSV persistence. |
| `src/test/java/com/lhn/client_fidelity/interfaces/rest/campaign/CampaignControllerTest.java` | **Create** — HTTP contract and auth behavior coverage. |

---

## 6. Acceptance Criteria

- [ ] All campaign routes require `Authorization: Bearer <jwt>`.
- [ ] A valid JWT whose `sub` resolves to a current `COMMERCE` user creates a campaign and returns `201 Created` with `Location: /campaigns/{campaignId}`.
- [ ] The campaign response body includes exactly `id`, `name`, `points`, `startDate`, `expirationDate`, `updatedAt`, and `template`.
- [ ] `GET /campaigns/{id}` returns `200 OK` for a campaign owned by the authenticated commerce.
- [ ] `GET /campaigns/me` returns `200 OK` with an array of campaigns owned by the authenticated commerce, and returns `[]` when none exist.
- [ ] `DELETE /campaigns/{id}` hard-deletes a campaign owned by the authenticated commerce and returns `204 No Content`.
- [ ] `PUT /campaigns/{id}` fully replaces editable fields and returns the updated campaign with a refreshed `updatedAt`.
- [ ] `PATCH /campaigns/{id}` partially updates editable fields and returns the updated campaign with a refreshed `updatedAt`.
- [ ] `points = 0` is accepted; negative values are rejected with `422 VALIDATION_FAILED`.
- [ ] `startDate` and `expirationDate` accept only ISO-8601 date strings, and `expirationDate < startDate` returns `422 VALIDATION_FAILED`.
- [ ] `template` accepts arbitrary nested fields only when the top-level value is a JSON object; non-object template values return `422 VALIDATION_FAILED`.
- [ ] Missing token returns `401 MISSING_ACCESS_TOKEN`.
- [ ] Invalid, malformed, expired, or stale token returns `401 INVALID_ACCESS_TOKEN`.
- [ ] A valid token for `COMMERCE_CLIENT` returns `403 FORBIDDEN_USER_TYPE`.
- [ ] Access to a nonexistent campaign id or a campaign owned by another commerce returns `404 CAMPAIGN_NOT_FOUND`.
- [ ] Unknown top-level JSON fields return `422 VALIDATION_FAILED`.
- [ ] `PATCH /campaigns/{id}` rejects an empty patch document with `422 VALIDATION_FAILED`.
- [ ] Campaign persistence works for both `h2` and `csv` repository modes selected through `client-fidelity.persistence.type`.
- [ ] `./mvnw test` passes without errors.

---

## 7. Additional considerations

- The route should avoid logging the raw JWT or template payload because both may contain sensitive or user-controlled data.
- The template is stored as an opaque object. The API should preserve the object content, but it does not guarantee textual formatting such as whitespace.
- If campaign updates are added later, the same `updatedAt` field should be reused as the server-managed last-modified timestamp rather than introducing a second mutable audit field.
- Because [RestExceptionHandler.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/interfaces/rest/error/RestExceptionHandler.java:35) currently maps most binding failures to `MALFORMED_JSON`, the campaign request should keep date fields as strings and validate them in the use case so invalid date formats produce `422 VALIDATION_FAILED` instead of a generic `400`.
