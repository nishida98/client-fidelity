package com.lhn.client_fidelity.interfaces.rest.authentication;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.lhn.client_fidelity.application.authentication.VerifyAuthenticationCodeCommand;
import com.lhn.client_fidelity.domain.user.FieldValidationError;
import com.lhn.client_fidelity.exception.AuthenticationValidationException;

import java.util.ArrayList;
import java.util.List;

public class VerifyAuthenticationCodeRequest {

	private String challengeId;
	private String code;
	private final List<String> unknownFields = new ArrayList<>();

	public String getChallengeId() {
		return challengeId;
	}

	public void setChallengeId(String challengeId) {
		this.challengeId = challengeId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@JsonAnySetter
	public void setUnknownField(String field, Object value) {
		unknownFields.add(field);
	}

	VerifyAuthenticationCodeCommand toCommand() {
		if (!unknownFields.isEmpty()) {
			throw new AuthenticationValidationException(unknownFields.stream()
					.map(field -> new FieldValidationError(field, "Field is not allowed."))
					.toList());
		}
		return new VerifyAuthenticationCodeCommand(challengeId, code);
	}
}
