package com.lhn.client_fidelity.interfaces.rest.error;

public record AuthenticationCodeAlreadySentResponse(
		String code,
		String message,
		long retryAfterSeconds
) {
}
