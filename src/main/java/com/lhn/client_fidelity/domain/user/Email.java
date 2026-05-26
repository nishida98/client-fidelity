package com.lhn.client_fidelity.domain.user;

import com.lhn.client_fidelity.exception.UserValidationException;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

	public Email {
		value = normalize(value);
		if (!EMAIL_PATTERN.matcher(value).matches()) {
			throw UserValidationException.single("email", "Email must be valid.");
		}
	}

	public static String normalize(String value) {
		if (value == null || value.trim().isEmpty()) {
			throw UserValidationException.single("email", "Email must not be blank.");
		}
		return value.trim().toLowerCase(Locale.ROOT);
	}
}
