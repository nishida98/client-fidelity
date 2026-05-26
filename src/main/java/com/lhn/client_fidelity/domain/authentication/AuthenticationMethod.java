package com.lhn.client_fidelity.domain.authentication;

import com.lhn.client_fidelity.exception.UnknownAuthenticationMethodException;

import java.util.Locale;

public enum AuthenticationMethod {
	EMAIL,
	PHONE;

	public static AuthenticationMethod parse(String value) {
		if (value == null || value.trim().isEmpty()) {
			throw new UnknownAuthenticationMethodException();
		}
		try {
			return AuthenticationMethod.valueOf(value.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException exception) {
			throw new UnknownAuthenticationMethodException();
		}
	}
}
