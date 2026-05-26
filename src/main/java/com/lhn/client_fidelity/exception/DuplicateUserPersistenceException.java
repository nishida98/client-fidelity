package com.lhn.client_fidelity.exception;

public class DuplicateUserPersistenceException extends RuntimeException {

	public DuplicateUserPersistenceException(Throwable cause) {
		super("User already exists.", cause);
	}

	public DuplicateUserPersistenceException() {
		super("User already exists.");
	}
}
