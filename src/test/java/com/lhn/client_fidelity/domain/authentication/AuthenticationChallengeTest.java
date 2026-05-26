package com.lhn.client_fidelity.domain.authentication;

import com.lhn.client_fidelity.domain.user.UserId;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationChallengeTest {

	private static final Instant NOW = Instant.parse("2026-05-25T23:30:00Z");

	@Test
	void createsChallengeWithExpiration() {
		AuthenticationChallenge challenge = AuthenticationChallenge.create(
				new UserId("usr_1"),
				AuthenticationMethod.EMAIL,
				"user@email.com",
				new AuthenticationCode("123456"),
				NOW,
				Duration.ofMinutes(5)
		);

		assertThat(challenge.expiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(5)));
		assertThat(challenge.isExpired(NOW.plus(Duration.ofMinutes(4)))).isFalse();
		assertThat(challenge.isExpired(NOW.plus(Duration.ofMinutes(5)))).isTrue();
	}

	@Test
	void consumesChallenge() {
		AuthenticationChallenge challenge = challenge();

		challenge.consume(NOW);

		assertThat(challenge.isConsumed()).isTrue();
		assertThat(challenge.consumedAt()).isEqualTo(NOW);
	}

	@Test
	void tracksFailedAttemptsAndLockState() {
		AuthenticationChallenge challenge = challenge();

		challenge.recordFailedAttempt();
		challenge.recordFailedAttempt();

		assertThat(challenge.attemptCount()).isEqualTo(2);
		assertThat(challenge.isLocked(2)).isTrue();
	}

	private AuthenticationChallenge challenge() {
		return AuthenticationChallenge.create(
				new UserId("usr_1"),
				AuthenticationMethod.EMAIL,
				"user@email.com",
				new AuthenticationCode("123456"),
				NOW,
				Duration.ofMinutes(5)
		);
	}
}
