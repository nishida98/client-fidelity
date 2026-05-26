package com.lhn.client_fidelity.exception;

public class InvalidAuthenticationCodeException extends RuntimeException {

	public InvalidAuthenticationCodeException() {
		super("Authentication code is invalid or expired.");
	}
}
