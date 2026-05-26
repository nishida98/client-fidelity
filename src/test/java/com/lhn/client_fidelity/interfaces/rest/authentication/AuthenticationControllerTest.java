package com.lhn.client_fidelity.interfaces.rest.authentication;

import com.lhn.client_fidelity.application.authentication.AuthenticationChallengeRepository;
import com.lhn.client_fidelity.application.authentication.AuthenticationCodeDelivery;
import com.lhn.client_fidelity.application.authentication.AuthenticationProperties;
import com.lhn.client_fidelity.application.authentication.RequestAuthenticationCodeUseCase;
import com.lhn.client_fidelity.application.authentication.TokenResult;
import com.lhn.client_fidelity.application.authentication.VerifyAuthenticationCodeUseCase;
import com.lhn.client_fidelity.application.user.UserRepository;
import com.lhn.client_fidelity.domain.authentication.AuthenticationChallenge;
import com.lhn.client_fidelity.domain.authentication.AuthenticationChallengeId;
import com.lhn.client_fidelity.domain.authentication.AuthenticationMethod;
import com.lhn.client_fidelity.domain.user.CommerceClient;
import com.lhn.client_fidelity.domain.user.Email;
import com.lhn.client_fidelity.domain.user.Phone;
import com.lhn.client_fidelity.domain.user.User;
import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.interfaces.rest.error.RestExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationControllerTest {

	private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-25T23:30:00Z"), ZoneOffset.UTC);

	private FakeUserRepository userRepository;
	private FakeChallengeRepository challengeRepository;
	private FakeDelivery delivery;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		userRepository = new FakeUserRepository();
		challengeRepository = new FakeChallengeRepository();
		delivery = new FakeDelivery();
		AuthenticationProperties properties = new AuthenticationProperties(
				"PT5M",
				"PT1H",
				5,
				"console",
				"not-configured",
				"secret"
		);
		RequestAuthenticationCodeUseCase requestUseCase = new RequestAuthenticationCodeUseCase(
				userRepository,
				challengeRepository,
				List.of(delivery),
				properties,
				CLOCK
		);
		VerifyAuthenticationCodeUseCase verifyUseCase = new VerifyAuthenticationCodeUseCase(
				challengeRepository,
				userRepository,
				(user, issuedAt) -> new TokenResult("Bearer", "token-value", issuedAt.plus(Duration.ofHours(1))),
				properties,
				CLOCK
		);
		mockMvc = MockMvcBuilders
				.standaloneSetup(new AuthenticationController(requestUseCase, verifyUseCase))
				.setControllerAdvice(new RestExceptionHandler(CLOCK))
				.build();
	}

	@Test
	void requestsEmailCode() throws Exception {
		userRepository.user = Optional.of(user());

		mockMvc.perform(post("/auth/codes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "method": "EMAIL",
								  "identifier": "USER@EMAIL.COM"
								}
								"""))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.challengeId").isNotEmpty())
				.andExpect(jsonPath("$.method").value("EMAIL"))
				.andExpect(jsonPath("$.expiresAt").exists());
	}

	@Test
	void returnsAcceptedForUnknownUser() throws Exception {
		mockMvc.perform(post("/auth/codes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "method": "EMAIL",
								  "identifier": "unknown@email.com"
								}
								"""))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.challengeId").doesNotExist());
	}

	@Test
	void returnsBadRequestForUnknownMethod() throws Exception {
		mockMvc.perform(post("/auth/codes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "method": "MAGIC",
								  "identifier": "user@email.com"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("UNKNOWN_AUTHENTICATION_METHOD"));
	}

	@Test
	void returnsUnprocessableEntityForInvalidIdentifier() throws Exception {
		mockMvc.perform(post("/auth/codes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "method": "EMAIL",
								  "identifier": "invalid"
								}
								"""))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
	}

	@Test
	void returnsTooManyRequestsForActiveCode() throws Exception {
		User user = user();
		userRepository.user = Optional.of(user);
		challengeRepository.active = Optional.of(AuthenticationChallenge.create(
				user.id(),
				AuthenticationMethod.EMAIL,
				"user@email.com",
				new com.lhn.client_fidelity.domain.authentication.AuthenticationCode("123456"),
				CLOCK.instant(),
				Duration.ofMinutes(5)
		));

		mockMvc.perform(post("/auth/codes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "method": "EMAIL",
								  "identifier": "user@email.com"
								}
								"""))
				.andExpect(status().isTooManyRequests())
				.andExpect(header().string("Retry-After", "300"))
				.andExpect(jsonPath("$.code").value("AUTH_CODE_ALREADY_SENT"));
	}

	@Test
	void verifiesCodeAndReturnsToken() throws Exception {
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

		mockMvc.perform(post("/auth/token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "challengeId": "%s",
								  "code": "123456"
								}
								""".formatted(challenge.id().value())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.accessToken").value("token-value"));
	}

	@Test
	void returnsUnauthorizedForInvalidCode() throws Exception {
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

		mockMvc.perform(post("/auth/token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "challengeId": "%s",
								  "code": "654321"
								}
								""".formatted(challenge.id().value())))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_AUTHENTICATION_CODE"));
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
		@Override
		public boolean supports(AuthenticationMethod method, String provider) {
			return method == AuthenticationMethod.EMAIL && "console".equals(provider);
		}

		@Override
		public void send(String destination, String code) {
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
