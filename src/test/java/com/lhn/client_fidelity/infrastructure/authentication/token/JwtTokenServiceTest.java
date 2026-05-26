package com.lhn.client_fidelity.infrastructure.authentication.token;

import com.lhn.client_fidelity.application.authentication.AuthenticationProperties;
import com.lhn.client_fidelity.application.authentication.TokenResult;
import com.lhn.client_fidelity.domain.user.CommerceClient;
import com.lhn.client_fidelity.domain.user.Email;
import com.lhn.client_fidelity.domain.user.Phone;
import com.lhn.client_fidelity.domain.user.UserId;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

	@Test
	void createsJwtToken() {
		AuthenticationProperties properties = new AuthenticationProperties(
				"PT5M",
				"PT1H",
				5,
				"console",
				"not-configured",
				"secret"
		);
		JwtTokenService tokenService = new JwtTokenService(properties);
		Instant issuedAt = Instant.parse("2026-05-25T23:30:00Z");

		TokenResult result = tokenService.createToken(
				new CommerceClient(
						new UserId("usr_1"),
						"User",
						new Email("user@email.com"),
						new Phone("11999999999"),
						issuedAt
				),
				issuedAt
		);

		assertThat(result.tokenType()).isEqualTo("Bearer");
		assertThat(result.accessToken()).contains(".");
		assertThat(result.expiresAt()).isEqualTo(issuedAt.plus(Duration.ofHours(1)));
	}
}
