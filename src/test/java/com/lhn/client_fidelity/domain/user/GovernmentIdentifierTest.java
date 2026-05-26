package com.lhn.client_fidelity.domain.user;

import com.lhn.client_fidelity.exception.UserValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GovernmentIdentifierTest {

	@Test
	void acceptsAndNormalizesValidCnpj() {
		assertThat(new GovernmentIdentifier("11.222.333/0001-81").value()).isEqualTo("11222333000181");
	}

	@ParameterizedTest
	@ValueSource(strings = {"1234567800019", "123456780001950", "00000000000000", "11.222.333/0001-80", " "})
	void rejectsInvalidCnpj(String value) {
		assertThatThrownBy(() -> new GovernmentIdentifier(value))
				.isInstanceOf(UserValidationException.class);
	}
}
