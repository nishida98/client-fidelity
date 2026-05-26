package com.lhn.client_fidelity.application.authentication;

public record VerifyAuthenticationCodeCommand(
		String challengeId,
		String code
) {
}
