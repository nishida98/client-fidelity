package com.lhn.client_fidelity.domain.user;

import com.lhn.client_fidelity.exception.UserValidationException;

import java.time.Instant;

public record CommerceClient(
		UserId id,
		String name,
		Email email,
		Phone phone,
		Instant createdAt
) implements User {

	public CommerceClient {
		name = requireName(name);
		if (id == null) {
			throw new IllegalArgumentException("User id must not be null");
		}
		if (email == null || phone == null || createdAt == null) {
			throw new IllegalArgumentException("Commerce client required fields must not be null");
		}
	}

	@Override
	public UserType type() {
		return UserType.COMMERCE_CLIENT;
	}

	private static String requireName(String value) {
		if (value == null || value.trim().isEmpty()) {
			throw UserValidationException.single("name", "Name must not be blank.");
		}
		return value.trim();
	}
}
