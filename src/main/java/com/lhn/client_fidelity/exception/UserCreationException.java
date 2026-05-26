package com.lhn.client_fidelity.exception;

public class UserCreationException extends RuntimeException {

	public UserCreationException(Throwable cause) {
		super("User could not be created.", cause);
	}
}
