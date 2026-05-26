package com.lhn.client_fidelity.domain.authentication;

import java.util.UUID;

public record AuthenticationChallengeId(String value) {

	public AuthenticationChallengeId {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Authentication challenge id must not be blank");
		}
	}

	public static AuthenticationChallengeId newId() {
		return new AuthenticationChallengeId("cha_" + UUID.randomUUID().toString().replace("-", ""));
	}
}
