package com.lhn.client_fidelity.application.authentication;

import com.lhn.client_fidelity.domain.authentication.AuthenticationMethod;

public interface AuthenticationCodeDelivery {

	boolean supports(AuthenticationMethod method, String provider);

	void send(String destination, String code);
}
