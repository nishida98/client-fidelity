package com.lhn.client_fidelity.infrastructure.authentication.delivery;

import com.lhn.client_fidelity.domain.authentication.AuthenticationMethod;
import com.lhn.client_fidelity.exception.PhoneDeliveryNotConfiguredException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNotConfiguredAuthenticationCodeDeliveryTest {

	@Test
	void throwsWhenPhoneProviderIsNotConfigured() {
		PhoneNotConfiguredAuthenticationCodeDelivery delivery = new PhoneNotConfiguredAuthenticationCodeDelivery();

		assertThat(delivery.supports(AuthenticationMethod.PHONE, "not-configured")).isTrue();
		assertThatThrownBy(() -> delivery.send("5511999999999", "123456"))
				.isInstanceOf(PhoneDeliveryNotConfiguredException.class);
	}
}
