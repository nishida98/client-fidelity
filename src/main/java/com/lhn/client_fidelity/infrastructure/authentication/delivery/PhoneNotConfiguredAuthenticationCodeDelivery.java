package com.lhn.client_fidelity.infrastructure.authentication.delivery;

import com.lhn.client_fidelity.application.authentication.AuthenticationCodeDelivery;
import com.lhn.client_fidelity.domain.authentication.AuthenticationMethod;
import com.lhn.client_fidelity.exception.PhoneDeliveryNotConfiguredException;
import org.springframework.stereotype.Component;

@Component
class PhoneNotConfiguredAuthenticationCodeDelivery implements AuthenticationCodeDelivery {

	@Override
	public boolean supports(AuthenticationMethod method, String provider) {
		return method == AuthenticationMethod.PHONE && "not-configured".equalsIgnoreCase(provider);
	}

	@Override
	public void send(String destination, String code) {
		throw new PhoneDeliveryNotConfiguredException();
	}
}
