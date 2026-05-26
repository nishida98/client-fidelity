package com.lhn.client_fidelity.exception;

public class AuthenticationDeliveryFailedException extends RuntimeException {

	public AuthenticationDeliveryFailedException(Throwable cause) {
		super("Authentication code could not be delivered.", cause);
	}

	public AuthenticationDeliveryFailedException() {
		super("Authentication code could not be delivered.");
	}
}
