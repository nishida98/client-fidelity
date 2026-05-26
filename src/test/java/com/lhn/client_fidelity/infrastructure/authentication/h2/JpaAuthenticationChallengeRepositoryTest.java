package com.lhn.client_fidelity.infrastructure.authentication.h2;

import com.lhn.client_fidelity.domain.authentication.AuthenticationChallenge;
import com.lhn.client_fidelity.domain.authentication.AuthenticationChallengeId;
import com.lhn.client_fidelity.domain.authentication.AuthenticationCode;
import com.lhn.client_fidelity.domain.authentication.AuthenticationMethod;
import com.lhn.client_fidelity.domain.user.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuthenticationChallengeRepository.class)
class JpaAuthenticationChallengeRepositoryTest {

	private static final Instant NOW = Instant.parse("2026-05-25T23:30:00Z");

	@Autowired
	private JpaAuthenticationChallengeRepository repository;

	@Test
	void savesAndLoadsChallenge() {
		AuthenticationChallenge challenge = activeChallenge();

		repository.save(challenge);

		assertThat(repository.findById(challenge.id()))
				.isPresent()
				.get()
				.extracting(AuthenticationChallenge::code)
				.isEqualTo(new AuthenticationCode("123456"));
	}

	@Test
	void findsOnlyActiveChallenge() {
		AuthenticationChallenge active = activeChallenge();
		repository.save(active);
		repository.save(expiredChallenge());

		assertThat(repository.findActiveByUserIdAndMethod(new UserId("usr_1"), AuthenticationMethod.EMAIL, NOW))
				.isPresent()
				.get()
				.extracting(AuthenticationChallenge::id)
				.isEqualTo(active.id());
	}

	@Test
	void excludesConsumedChallengeFromActiveLookup() {
		AuthenticationChallenge consumed = activeChallenge();
		consumed.consume(NOW);
		repository.save(consumed);

		assertThat(repository.findActiveByUserIdAndMethod(new UserId("usr_1"), AuthenticationMethod.EMAIL, NOW))
				.isEmpty();
	}

	private AuthenticationChallenge activeChallenge() {
		return AuthenticationChallenge.create(
				new UserId("usr_1"),
				AuthenticationMethod.EMAIL,
				"user@email.com",
				new AuthenticationCode("123456"),
				NOW,
				Duration.ofMinutes(5)
		);
	}

	private AuthenticationChallenge expiredChallenge() {
		return new AuthenticationChallenge(
				AuthenticationChallengeId.newId(),
				new UserId("usr_1"),
				AuthenticationMethod.EMAIL,
				"user@email.com",
				new AuthenticationCode("654321"),
				NOW.minusSeconds(1),
				null,
				0,
				NOW.minusSeconds(301)
		);
	}
}
