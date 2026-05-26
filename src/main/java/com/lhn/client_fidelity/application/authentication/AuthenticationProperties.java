package com.lhn.client_fidelity.application.authentication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthenticationProperties {

	private final Duration codeExpiration;
	private final Duration jwtExpiration;
	private final int maxFailedAttempts;
	private final String emailProvider;
	private final String phoneProvider;
	private final String jwtSecret;

	public AuthenticationProperties(
			@Value("${client-fidelity.authentication.code-expiration:PT5M}") String codeExpiration,
			@Value("${client-fidelity.authentication.jwt-expiration:PT1H}") String jwtExpiration,
			@Value("${client-fidelity.authentication.max-failed-attempts:5}") int maxFailedAttempts,
			@Value("${client-fidelity.authentication.email-provider:console}") String emailProvider,
			@Value("${client-fidelity.authentication.phone-provider:not-configured}") String phoneProvider,
			@Value("${client-fidelity.authentication.jwt-secret}") String jwtSecret
	) {
		this.codeExpiration = Duration.parse(codeExpiration);
		this.jwtExpiration = Duration.parse(jwtExpiration);
		this.maxFailedAttempts = maxFailedAttempts;
		this.emailProvider = emailProvider;
		this.phoneProvider = phoneProvider;
		this.jwtSecret = jwtSecret;
	}

	public Duration codeExpiration() {
		return codeExpiration;
	}

	public Duration jwtExpiration() {
		return jwtExpiration;
	}

	public int maxFailedAttempts() {
		return maxFailedAttempts;
	}

	public String providerFor(com.lhn.client_fidelity.domain.authentication.AuthenticationMethod method) {
		return method == com.lhn.client_fidelity.domain.authentication.AuthenticationMethod.EMAIL
				? emailProvider
				: phoneProvider;
	}

	public String jwtSecret() {
		return jwtSecret;
	}
}
