package com.lhn.client_fidelity.exception;

public class MissingAccessTokenException extends RuntimeException {

	public MissingAccessTokenException() {
		super("Authorization header with Bearer token is required.");
	}
}
