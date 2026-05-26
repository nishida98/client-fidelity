package com.lhn.client_fidelity.exception;

public class UnknownAuthenticationMethodException extends RuntimeException {

	public UnknownAuthenticationMethodException() {
		super("Authentication method must be EMAIL or PHONE.");
	}
}
