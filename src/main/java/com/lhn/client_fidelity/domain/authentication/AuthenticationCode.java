package com.lhn.client_fidelity.domain.authentication;

import com.lhn.client_fidelity.exception.AuthenticationValidationException;

import java.security.SecureRandom;

public record AuthenticationCode(String value) {

	private static final SecureRandom RANDOM = new SecureRandom();

	public AuthenticationCode {
		if (value == null || value.isBlank()) {
			throw AuthenticationValidationException.single("code", "Code must not be blank.");
		}
		if (!value.matches("\\d{6}")) {
			throw AuthenticationValidationException.single("code", "Code must be a 6-digit numeric value.");
		}
	}

	public static AuthenticationCode generate() {
		return new AuthenticationCode(String.format("%06d", RANDOM.nextInt(1_000_000)));
	}
}
