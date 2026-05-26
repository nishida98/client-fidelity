package com.lhn.client_fidelity.infrastructure.authentication.delivery;

import com.lhn.client_fidelity.application.authentication.AuthenticationCodeDelivery;
import com.lhn.client_fidelity.domain.authentication.AuthenticationMethod;
import org.springframework.stereotype.Component;

@Component
class ConsoleEmailAuthenticationCodeDelivery implements AuthenticationCodeDelivery {

	@Override
	public boolean supports(AuthenticationMethod method, String provider) {
		return method == AuthenticationMethod.EMAIL && "console".equalsIgnoreCase(provider);
	}

	@Override
	public void send(String destination, String code) {
		System.out.printf("Authentication code for %s: %s%n", destination, code);
	}
}
