package com.lhn.client_fidelity.exception;

public class TokenCreationException extends RuntimeException {

	public TokenCreationException(Throwable cause) {
		super("Authentication token could not be created.", cause);
	}
}
