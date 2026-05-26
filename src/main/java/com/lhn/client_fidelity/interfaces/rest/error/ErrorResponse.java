package com.lhn.client_fidelity.interfaces.rest.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
		String code,
		String message,
		List<ErrorDetailResponse> details
) {

	public static ErrorResponse withoutDetails(String code, String message) {
		return new ErrorResponse(code, message, null);
	}
}
