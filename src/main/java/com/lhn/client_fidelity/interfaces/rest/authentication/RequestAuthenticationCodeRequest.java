package com.lhn.client_fidelity.interfaces.rest.authentication;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.lhn.client_fidelity.application.authentication.RequestAuthenticationCodeCommand;
import com.lhn.client_fidelity.domain.user.FieldValidationError;
import com.lhn.client_fidelity.exception.AuthenticationValidationException;

import java.util.ArrayList;
import java.util.List;

public class RequestAuthenticationCodeRequest {

	private String method;
	private String identifier;
	private final List<String> unknownFields = new ArrayList<>();

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@JsonAnySetter
	public void setUnknownField(String field, Object value) {
		unknownFields.add(field);
	}

	RequestAuthenticationCodeCommand toCommand() {
		if (!unknownFields.isEmpty()) {
			throw new AuthenticationValidationException(unknownFields.stream()
					.map(field -> new FieldValidationError(field, "Field is not allowed."))
					.toList());
		}
		return new RequestAuthenticationCodeCommand(method, identifier);
	}
}
