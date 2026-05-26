package com.lhn.client_fidelity.application.authentication;

import java.time.Instant;

public record TokenResult(
		String tokenType,
		String accessToken,
		Instant expiresAt
) {
}
