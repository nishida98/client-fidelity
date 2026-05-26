package com.lhn.client_fidelity.application.authentication;

public record RequestAuthenticationCodeCommand(
		String method,
		String identifier
) {
}
