package com.lhn.client_fidelity.domain.user;

import com.lhn.client_fidelity.exception.UserValidationException;

public record GovernmentIdentifier(String value) {

	private static final CnpjValidator CNPJ_VALIDATOR = new CnpjValidator();

	public GovernmentIdentifier {
		value = normalize(value);
		CNPJ_VALIDATOR.validate(value);
	}

	public static String normalize(String value) {
		if (value == null || value.trim().isEmpty()) {
			throw UserValidationException.single("governmentIdentifier", "Government identifier must not be blank.");
		}
		String digits = value.trim().replaceAll("\\D", "");
		if (digits.length() != 14) {
			throw UserValidationException.single("governmentIdentifier", "Government identifier must contain 14 digits.");
		}
		return digits;
	}
}
