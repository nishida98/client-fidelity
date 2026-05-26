package com.lhn.client_fidelity.interfaces.rest.authentication;

import com.lhn.client_fidelity.application.authentication.TokenResult;

import java.time.Instant;

public record TokenResponse(
		String tokenType,
		String accessToken,
		Instant expiresAt
) {

	static TokenResponse from(TokenResult result) {
		return new TokenResponse(result.tokenType(), result.accessToken(), result.expiresAt());
	}
}
