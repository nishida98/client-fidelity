package com.lhn.client_fidelity.domain.user;

import java.util.UUID;

public record UserId(String value) {

	public UserId {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("User id must not be blank");
		}
	}

	public static UserId newId() {
		return new UserId("usr_" + UUID.randomUUID().toString().replace("-", ""));
	}
}
