package com.lhn.client_fidelity.interfaces.rest.authentication;

import com.lhn.client_fidelity.application.authentication.AccessTokenVerifier;
import com.lhn.client_fidelity.application.authentication.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class JwtAuthenticationInterceptor implements HandlerInterceptor {

	public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser";

	private final AccessTokenVerifier accessTokenVerifier;

	public JwtAuthenticationInterceptor(AccessTokenVerifier accessTokenVerifier) {
		this.accessTokenVerifier = accessTokenVerifier;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		AuthenticatedUser authenticatedUser = accessTokenVerifier.verify(request.getHeader("Authorization"));
		request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, authenticatedUser);
		return true;
	}
}
