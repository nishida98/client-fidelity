package com.lhn.client_fidelity.domain.authentication;

import com.lhn.client_fidelity.exception.AuthenticationValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticationCodeTest {

	@Test
	void generatesSixDigitCode() {
		assertThat(AuthenticationCode.generate().value()).matches("\\d{6}");
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "12345", "1234567", "abcdef", "12345a"})
	void rejectsInvalidCode(String code) {
		assertThatThrownBy(() -> new AuthenticationCode(code))
				.isInstanceOf(AuthenticationValidationException.class);
	}
}
