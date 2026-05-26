package com.lhn.client_fidelity.domain.user;

import com.lhn.client_fidelity.exception.UserValidationException;

import java.time.Instant;

public record Commerce(
		UserId id,
		String name,
		String contactName,
		Email email,
		Phone phone,
		GovernmentIdentifier governmentIdentifier,
		Instant createdAt
) implements User {

	public Commerce {
		name = requireText("name", name, "Name must not be blank.");
		contactName = requireText("contactName", contactName, "Contact name must not be blank.");
		if (id == null) {
			throw new IllegalArgumentException("User id must not be null");
		}
		if (email == null || phone == null || governmentIdentifier == null || createdAt == null) {
			throw new IllegalArgumentException("Commerce required fields must not be null");
		}
	}

	@Override
	public UserType type() {
		return UserType.COMMERCE;
	}

	private static String requireText(String field, String value, String message) {
		if (value == null || value.trim().isEmpty()) {
			throw UserValidationException.single(field, message);
		}
		return value.trim();
	}
}
