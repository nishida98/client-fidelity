package com.lhn.client_fidelity.exception;

public class CommerceClientAlreadyExistsException extends RuntimeException {

	public CommerceClientAlreadyExistsException() {
		super("Commerce client already exists for the provided email.");
	}
}
