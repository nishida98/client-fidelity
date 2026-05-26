package com.lhn.client_fidelity.interfaces.rest.authentication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lhn.client_fidelity.application.authentication.RequestAuthenticationCodeResult;
import com.lhn.client_fidelity.domain.authentication.AuthenticationMethod;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RequestAuthenticationCodeResponse(
		String challengeId,
		AuthenticationMethod method,
		Instant expiresAt
) {

	static RequestAuthenticationCodeResponse from(RequestAuthenticationCodeResult result) {
		return new RequestAuthenticationCodeResponse(result.challengeId(), result.method(), result.expiresAt());
	}
}
