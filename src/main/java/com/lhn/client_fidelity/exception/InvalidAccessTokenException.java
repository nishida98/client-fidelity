package com.lhn.client_fidelity.exception;

public class InvalidAccessTokenException extends RuntimeException {

	public InvalidAccessTokenException() {
		super("Access token is invalid or expired.");
	}

	public InvalidAccessTokenException(Throwable cause) {
		super("Access token is invalid or expired.", cause);
	}
}
