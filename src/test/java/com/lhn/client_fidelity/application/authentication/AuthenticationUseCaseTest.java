package com.lhn.client_fidelity.application.authentication;

import com.lhn.client_fidelity.application.user.UserRepository;
import com.lhn.client_fidelity.domain.authentication.AuthenticationChallenge;
import com.lhn.client_fidelity.domain.authentication.AuthenticationChallengeId;
import com.lhn.client_fidelity.domain.authentication.AuthenticationMethod;
import com.lhn.client_fidelity.domain.user.CommerceClient;
import com.lhn.client_fidelity.domain.user.Email;
import com.lhn.client_fidelity.domain.user.Phone;
import com.lhn.client_fidelity.domain.user.User;
import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.exception.AuthenticationCodeAlreadySentException;
import com.lhn.client_fidelity.exception.InvalidAuthenticationCodeException;
import com.lhn.client_fidelity.exception.UnknownAuthenticationMethodException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticationUseCaseTest {

	private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-25T23:30:00Z"), ZoneOffset.UTC);

	private final FakeUserRepository userRepository = new FakeUserRepository();
	private final FakeChallengeRepository challengeRepository = new FakeChallengeRepository();
	private final FakeDelivery delivery = new FakeDelivery();
	private final AuthenticationProperties properties = new AuthenticationProperties(
			"PT5M",
			"PT1H",
			5,
			"console",
			"not-configured",
			"secret"
	);

	@Test
	void requestsEmailCodeForExistingUser() {
		userRepository.user = Optional.of(user());
		RequestAuthenticationCodeUseCase useCase = requestUseCase();

		RequestAuthenticationCodeResult result = useCase.execute(new RequestAuthenticationCodeCommand("EMAIL", "USER@EMAIL.COM"));

		assertThat(result.challengeId()).isNotBlank();
		assertThat(result.method()).isEqualTo(AuthenticationMethod.EMAIL);
		assertThat(result.expiresAt()).isEqualTo(CLOCK.instant().plus(Duration.ofMinutes(5)));
		assertThat(challengeRepository.saved.code().value()).matches("\\d{6}");
		assertThat(delivery.sentCode).isEqualTo(challengeRepository.saved.code().value());
		assertThat(delivery.destination).isEqualTo("user@email.com");
	}

	@Test
	void returnsAcceptedWithoutChallengeForUnknownUser() {
		RequestAuthenticationCodeResult result = requestUseCase()
				.execute(new RequestAuthenticationCodeCommand("EMAIL", "unknown@email.com"));

		assertThat(result.challengeId()).isNull();
		assertThat(delivery.sentCode).isNull();
		assertThat(challengeRepository.saved).isNull();
	}

	@Test
	void rejectsUnknownAuthenticationMethod() {
		assertThatThrownBy(() -> requestUseCase().execute(new RequestAuthenticationCodeCommand("magic", "user@email.com")))
				.isInstanceOf(UnknownAuthenticationMethodException.class);
	}

	@Test
	void rejectsActiveDuplicateCodeRequest() {
		userRepository.user = Optional.of(user());
		challengeRepository.active = Optional.of(AuthenticationChallenge.create(
				user().id(),
				AuthenticationMethod.EMAIL,
				"user@email.com",
				new com.lhn.client_fidelity.domain.authentication.AuthenticationCode("123456"),
				CLOCK.instant(),
				Duration.ofMinutes(5)
		));

		assertThatThrownBy(() -> requestUseCase().execute(new RequestAuthenticationCodeCommand("EMAIL", "user@email.com")))
				.isInstanceOf(AuthenticationCodeAlreadySentException.class);
	}

	@Test
	void verifiesCorrectCodeAndConsumesChallenge() {
		User user = user();
		userRepository.user = Optional.of(user);
		AuthenticationChallenge challenge = AuthenticationChallenge.create(
				user.id(),
				AuthenticationMethod.EMAIL,
				"user@email.com",
				new com.lhn.client_fidelity.domain.authentication.AuthenticationCode("123456"),
				CLOCK.instant(),
				Duration.ofMinutes(5)
		);
		challengeRepository.saved = challenge;
		VerifyAuthenticationCodeUseCase useCase = verifyUseCase();

		TokenResult token = useCase.execute(new VerifyAuthenticationCodeCommand(challenge.id().value(), "123456"));

		assertThat(token.tokenType()).isEqualTo("Bearer");
		assertThat(challengeRepository.saved.isConsumed()).isTrue();
	}

	@Test
	void rejectsIncorrectCodeAndTracksAttempt() {
		User user = user();
		userRepository.user = Optional.of(user);
		AuthenticationChallenge challenge = AuthenticationChallenge.create(
				user.id(),
				AuthenticationMethod.EMAIL,
				"user@email.com",
				new com.lhn.client_fidelity.domain.authentication.AuthenticationCode("123456"),
				CLOCK.instant(),
				Duration.ofMinutes(5)
		);
		challengeRepository.saved = challenge;

		assertThatThrownBy(() -> verifyUseCase().execute(new VerifyAuthenticationCodeCommand(challenge.id().value(), "654321")))
				.isInstanceOf(InvalidAuthenticationCodeException.class);
		assertThat(challengeRepository.saved.attemptCount()).isEqualTo(1);
	}

	@Test
	void rejectsExpiredCode() {
		User user = user();
		userRepository.user = Optional.of(user);
		AuthenticationChallenge challenge = new AuthenticationChallenge(
				AuthenticationChallengeId.newId(),
				user.id(),
				AuthenticationMethod.EMAIL,
				"user@email.com",
				new com.lhn.client_fidelity.domain.authentication.AuthenticationCode("123456"),
				CLOCK.instant().minusSeconds(1),
				null,
				0,
				CLOCK.instant().minusSeconds(301)
		);
		challengeRepository.saved = challenge;

		assertThatThrownBy(() -> verifyUseCase().execute(new VerifyAuthenticationCodeCommand(challenge.id().value(), "123456")))
				.isInstanceOf(InvalidAuthenticationCodeException.class);
	}

	private RequestAuthenticationCodeUseCase requestUseCase() {
		return new RequestAuthenticationCodeUseCase(
				userRepository,
				challengeRepository,
				List.of(delivery),
				properties,
				CLOCK
		);
	}

	private VerifyAuthenticationCodeUseCase verifyUseCase() {
		return new VerifyAuthenticationCodeUseCase(
				challengeRepository,
				userRepository,
				(user, issuedAt) -> new TokenResult("Bearer", "token", issuedAt.plus(Duration.ofHours(1))),
				properties,
				CLOCK
		);
	}

	private User user() {
		return new CommerceClient(
				new UserId("usr_1"),
				"User",
				new Email("user@email.com"),
				new Phone("11999999999"),
				CLOCK.instant()
		);
	}

	private static class FakeDelivery implements AuthenticationCodeDelivery {

		private String destination;
		private String sentCode;

		@Override
		public boolean supports(AuthenticationMethod method, String provider) {
			return method == AuthenticationMethod.EMAIL && "console".equals(provider);
		}

		@Override
		public void send(String destination, String code) {
			this.destination = destination;
			this.sentCode = code;
		}
	}

	private static class FakeChallengeRepository implements AuthenticationChallengeRepository {

		private AuthenticationChallenge saved;
		private Optional<AuthenticationChallenge> active = Optional.empty();

		@Override
		public AuthenticationChallenge save(AuthenticationChallenge challenge) {
			saved = challenge;
			return challenge;
		}

		@Override
		public Optional<AuthenticationChallenge> findById(AuthenticationChallengeId id) {
			if (saved != null && saved.id().equals(id)) {
				return Optional.of(saved);
			}
			return Optional.empty();
		}

		@Override
		public Optional<AuthenticationChallenge> findActiveByUserIdAndMethod(
				UserId userId,
				AuthenticationMethod method,
				Instant now
		) {
			return active;
		}
	}

	private static class FakeUserRepository implements UserRepository {

		private Optional<User> user = Optional.empty();

		@Override
		public boolean existsCommerceByGovernmentIdentifier(String governmentIdentifier) {
			return false;
		}

		@Override
		public boolean existsCommerceClientByEmail(String email) {
			return false;
		}

		@Override
		public Optional<User> findByEmail(String email) {
			return user;
		}

		@Override
		public Optional<User> findByPhone(String phone) {
			return user;
		}

		@Override
		public User save(User user) {
			return user;
		}
	}
}
