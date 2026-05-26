package com.lhn.client_fidelity.infrastructure.authentication.delivery;

import com.lhn.client_fidelity.application.authentication.AuthenticationCodeDelivery;
import com.lhn.client_fidelity.domain.authentication.AuthenticationMethod;
import com.lhn.client_fidelity.exception.AuthenticationDeliveryFailedException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "client-fidelity.authentication.email-provider", havingValue = "ses")
class SesEmailAuthenticationCodeDelivery implements AuthenticationCodeDelivery {

	@Override
	public boolean supports(AuthenticationMethod method, String provider) {
		return method == AuthenticationMethod.EMAIL && "ses".equalsIgnoreCase(provider);
	}

	@Override
	public void send(String destination, String code) {
		throw new AuthenticationDeliveryFailedException();
	}
}
