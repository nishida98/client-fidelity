package com.lhn.client_fidelity.exception;

import com.lhn.client_fidelity.domain.user.FieldValidationError;

import java.util.List;

public class UserValidationException extends RuntimeException {

	private final List<FieldValidationError> errors;

	public UserValidationException(List<FieldValidationError> errors) {
		super("The request contains invalid fields.");
		this.errors = List.copyOf(errors);
	}

	public static UserValidationException single(String field, String message) {
		return new UserValidationException(List.of(new FieldValidationError(field, message)));
	}

	public List<FieldValidationError> errors() {
		return errors;
	}
}
