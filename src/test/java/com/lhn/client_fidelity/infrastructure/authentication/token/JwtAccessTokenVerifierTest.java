package com.lhn.client_fidelity.infrastructure.authentication.token;

import com.lhn.client_fidelity.application.authentication.AuthenticatedUser;
import com.lhn.client_fidelity.application.authentication.AuthenticationProperties;
import com.lhn.client_fidelity.application.user.UserRepository;
import com.lhn.client_fidelity.domain.user.CommerceClient;
import com.lhn.client_fidelity.domain.user.Email;
import com.lhn.client_fidelity.domain.user.Phone;
import com.lhn.client_fidelity.domain.user.User;
import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.exception.InvalidAccessTokenException;
import com.lhn.client_fidelity.exception.MissingAccessTokenException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtAccessTokenVerifierTest {

	private static final Instant ISSUED_AT = Instant.parse("2026-05-25T23:30:00Z");
	private static final Clock CLOCK = Clock.fixed(ISSUED_AT, ZoneOffset.UTC);

	@Test
	void verifiesValidToken() {
		AuthenticationProperties properties = properties();
		User user = user();
		FakeUserRepository repository = new FakeUserRepository(Optional.of(user));
		JwtTokenService tokenService = new JwtTokenService(properties);
		String token = tokenService.createToken(user, ISSUED_AT).accessToken();
		JwtAccessTokenVerifier verifier = new JwtAccessTokenVerifier(properties, repository, CLOCK);

		AuthenticatedUser authenticatedUser = verifier.verify("Bearer " + token);

		assertThat(authenticatedUser.userId()).isEqualTo(user.id());
		assertThat(authenticatedUser.userType()).isEqualTo(user.type());
	}

	@Test
	void rejectsMissingHeader() {
		JwtAccessTokenVerifier verifier = new JwtAccessTokenVerifier(properties(), new FakeUserRepository(Optional.of(user())), CLOCK);

		assertThatThrownBy(() -> verifier.verify(null))
				.isInstanceOf(MissingAccessTokenException.class);
	}

	@Test
	void rejectsNonBearerHeader() {
		JwtAccessTokenVerifier verifier = new JwtAccessTokenVerifier(properties(), new FakeUserRepository(Optional.of(user())), CLOCK);

		assertThatThrownBy(() -> verifier.verify("Basic token"))
				.isInstanceOf(InvalidAccessTokenException.class);
	}

	@Test
	void rejectsExpiredToken() {
		AuthenticationProperties properties = properties();
		User user = user();
		String token = new JwtTokenService(properties).createToken(user, ISSUED_AT).accessToken();
		Clock expiredClock = Clock.fixed(ISSUED_AT.plusSeconds(3601), ZoneOffset.UTC);
		JwtAccessTokenVerifier verifier = new JwtAccessTokenVerifier(
				properties,
				new FakeUserRepository(Optional.of(user)),
				expiredClock
		);

		assertThatThrownBy(() -> verifier.verify("Bearer " + token))
				.isInstanceOf(InvalidAccessTokenException.class);
	}

	@Test
	void rejectsTokenForMissingUser() {
		AuthenticationProperties properties = properties();
		User user = user();
		String token = new JwtTokenService(properties).createToken(user, ISSUED_AT).accessToken();
		JwtAccessTokenVerifier verifier = new JwtAccessTokenVerifier(
				properties,
				new FakeUserRepository(Optional.empty()),
				CLOCK
		);

		assertThatThrownBy(() -> verifier.verify("Bearer " + token))
				.isInstanceOf(InvalidAccessTokenException.class);
	}

	private AuthenticationProperties properties() {
		return new AuthenticationProperties("PT5M", "PT1H", 5, "console", "not-configured", "secret");
	}

	private User user() {
		return new CommerceClient(
				new UserId("usr_1"),
				"User",
				new Email("user@email.com"),
				new Phone("11999999999"),
				ISSUED_AT
		);
	}

	private static class FakeUserRepository implements UserRepository {

		private final Optional<User> user;

		private FakeUserRepository(Optional<User> user) {
			this.user = user;
		}

		@Override
		public boolean existsCommerceByGovernmentIdentifier(String governmentIdentifier) {
			return false;
		}

		@Override
		public boolean existsCommerceClientByEmail(String email) {
			return false;
		}

		@Override
		public Optional<User> findById(UserId id) {
			return user.filter(value -> value.id().equals(id));
		}

		@Override
		public Optional<User> findByEmail(String email) {
			return Optional.empty();
		}

		@Override
		public Optional<User> findByPhone(String phone) {
			return Optional.empty();
		}

		@Override
		public User save(User user) {
			return user;
		}
	}
}
