package com.lhn.client_fidelity.exception;

import com.lhn.client_fidelity.domain.user.FieldValidationError;

import java.util.List;

public class CampaignValidationException extends RuntimeException {

	private final List<FieldValidationError> errors;

	public CampaignValidationException(List<FieldValidationError> errors) {
		super("The request contains invalid fields.");
		this.errors = List.copyOf(errors);
	}

	public static CampaignValidationException single(String field, String message) {
		return new CampaignValidationException(List.of(new FieldValidationError(field, message)));
	}

	public List<FieldValidationError> errors() {
		return errors;
	}
}
