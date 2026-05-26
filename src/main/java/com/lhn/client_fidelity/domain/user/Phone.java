package com.lhn.client_fidelity.domain.user;

import com.lhn.client_fidelity.exception.UserValidationException;

import java.util.regex.Pattern;

public record Phone(String value) {

	private static final Pattern ALLOWED_INPUT = Pattern.compile("^\\+?[0-9\\s()\\-]+$");

	public Phone {
		value = normalize(value);
	}

	public static String normalize(String value) {
		if (value == null || value.trim().isEmpty()) {
			throw UserValidationException.single("phone", "Phone must not be blank.");
		}
		String trimmed = value.trim();
		if (!ALLOWED_INPUT.matcher(trimmed).matches()) {
			throw UserValidationException.single("phone", "Phone must be valid.");
		}
		String digits = trimmed.replaceAll("\\D", "");
		if (digits.length() < 10 || digits.length() > 15) {
			throw UserValidationException.single("phone", "Phone must contain between 10 and 15 digits.");
		}
		return digits;
	}
}
