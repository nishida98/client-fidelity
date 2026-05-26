package com.lhn.client_fidelity.exception;

public class CommerceAlreadyExistsException extends RuntimeException {

	public CommerceAlreadyExistsException() {
		super("Commerce already exists for the provided government identifier.");
	}
}
