package com.lhn.client_fidelity.exception;

import java.time.Instant;

public class AuthenticationCodeAlreadySentException extends RuntimeException {

	private final Instant retryAfter;

	public AuthenticationCodeAlreadySentException(Instant retryAfter) {
		super("An authentication code was already sent. Try again after it expires.");
		this.retryAfter = retryAfter;
	}

	public Instant retryAfter() {
		return retryAfter;
	}
}
