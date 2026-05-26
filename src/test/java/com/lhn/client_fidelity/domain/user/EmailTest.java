package com.lhn.client_fidelity.domain.user;

import com.lhn.client_fidelity.exception.UserValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

	@Test
	void normalizesEmailToLowercase() {
		assertThat(new Email(" CUSTOMER@EMAIL.COM ").value()).isEqualTo("customer@email.com");
	}

	@ParameterizedTest
	@ValueSource(strings = {"customer@", "@email.com", "customeremail.com", "customer@localhost", "customer @email.com", "a@b@c.com"})
	void rejectsInvalidEmail(String value) {
		assertThatThrownBy(() -> new Email(value))
				.isInstanceOf(UserValidationException.class);
	}

	@Test
	void rejectsBlankEmail() {
		assertThatThrownBy(() -> new Email(" "))
				.isInstanceOf(UserValidationException.class);
	}
}
