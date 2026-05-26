package com.lhn.client_fidelity.exception;

public class UnknownUserTypeException extends RuntimeException {

	public UnknownUserTypeException() {
		super("User type must be COMMERCE or COMMERCE_CLIENT.");
	}
}
