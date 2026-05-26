package com.lhn.client_fidelity.application.authentication;

import com.lhn.client_fidelity.domain.authentication.AuthenticationMethod;

import java.time.Instant;

public record RequestAuthenticationCodeResult(
		String challengeId,
		AuthenticationMethod method,
		Instant expiresAt
) {

	public static RequestAuthenticationCodeResult acceptedWithoutChallenge(AuthenticationMethod method) {
		return new RequestAuthenticationCodeResult(null, method, null);
	}
}
