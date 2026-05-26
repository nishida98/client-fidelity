# TASK-4 — Protect Campaign Routes With An Interceptor

**Target file:** `src/main/java/com/lhn/client_fidelity/interfaces/rest/authentication/WebMvcAuthenticationConfiguration.java` (new)
**SDD reference:** Section 3.2
**Depends on:** TASK-3
**Blocked by:** TASK-3

---

## Context

The current REST controllers in [UserController.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/interfaces/rest/user/UserController.java:13) and [AuthenticationController.java](/C:/_development/client-fidelity/src/main/java/com/lhn/client_fidelity/interfaces/rest/authentication/AuthenticationController.java:13) do not consume JWTs at all. The SDD keeps auth focused by protecting only `/campaigns/**`, without introducing global Spring Security configuration.

## What to do

Add a dedicated MVC interceptor and register it only for campaign routes.

Create the interceptor:

```java
public final class JwtAuthenticationInterceptor implements HandlerInterceptor {

    public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser";

    private final AccessTokenVerifier accessTokenVerifier;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        AuthenticatedUser authenticatedUser =
                accessTokenVerifier.verify(request.getHeader("Authorization"));
        request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, authenticatedUser);
        return true;
    }
}
```

Register it through `WebMvcConfigurer`:

```java
@Configuration
public class WebMvcAuthenticationConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor).addPathPatterns("/campaigns/**");
    }
}
```

## Implementation notes

Do not attach the interceptor to `/users` or `/auth/**`. Keep the request attribute name as a constant so the future campaign controller can retrieve the authenticated principal safely.

## Acceptance criteria

- [ ] `/campaigns/**` is protected by the JWT interceptor.
- [ ] The interceptor stores `AuthenticatedUser` in the request.
- [ ] Existing `/users` and `/auth/**` routes remain unprotected.

