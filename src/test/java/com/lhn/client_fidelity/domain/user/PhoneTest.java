package com.lhn.client_fidelity.domain.user;

import com.lhn.client_fidelity.exception.UserValidationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneTest {

	@ParameterizedTest
	@CsvSource({
			"+5511999999999,5511999999999",
			"(11) 99999-9999,11999999999",
			"11999999999,11999999999"
	})
	void acceptsAndNormalizesPhone(String input, String expected) {
		assertThat(new Phone(input).value()).isEqualTo(expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {"12345", "+55 11 phone", "+55@11999999999", "1234567890123456", " "})
	void rejectsInvalidPhone(String value) {
		assertThatThrownBy(() -> new Phone(value))
				.isInstanceOf(UserValidationException.class);
	}
}
