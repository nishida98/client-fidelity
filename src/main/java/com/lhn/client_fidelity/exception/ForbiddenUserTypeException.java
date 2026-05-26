package com.lhn.client_fidelity.exception;

public class ForbiddenUserTypeException extends RuntimeException {

	public ForbiddenUserTypeException() {
		super("Only commerce users can manage campaigns.");
	}
}
