package com.lhn.client_fidelity.application.authentication;

import com.lhn.client_fidelity.application.user.UserRepository;
import com.lhn.client_fidelity.domain.authentication.AuthenticationChallenge;
import com.lhn.client_fidelity.domain.authentication.AuthenticationChallengeId;
import com.lhn.client_fidelity.domain.authentication.AuthenticationCode;
import com.lhn.client_fidelity.domain.user.User;
import com.lhn.client_fidelity.exception.AuthenticationValidationException;
import com.lhn.client_fidelity.exception.InvalidAuthenticationCodeException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class VerifyAuthenticationCodeUseCase {

	private final AuthenticationChallengeRepository challengeRepository;
	private final UserRepository userRepository;
	private final TokenService tokenService;
	private final AuthenticationProperties properties;
	private final Clock clock;

	public VerifyAuthenticationCodeUseCase(
			AuthenticationChallengeRepository challengeRepository,
			UserRepository userRepository,
			TokenService tokenService,
			AuthenticationProperties properties,
			Clock clock
	) {
		this.challengeRepository = challengeRepository;
		this.userRepository = userRepository;
		this.tokenService = tokenService;
		this.properties = properties;
		this.clock = clock;
	}

	public TokenResult execute(VerifyAuthenticationCodeCommand command) {
		AuthenticationChallengeId challengeId = parseChallengeId(command.challengeId());
		AuthenticationCode submittedCode = new AuthenticationCode(command.code());
		AuthenticationChallenge challenge = challengeRepository.findById(challengeId)
				.orElseThrow(InvalidAuthenticationCodeException::new);
		Instant now = clock.instant();
		if (challenge.isExpired(now) || challenge.isConsumed() || challenge.isLocked(properties.maxFailedAttempts())) {
			throw new InvalidAuthenticationCodeException();
		}
		if (!challenge.matches(submittedCode)) {
			challenge.recordFailedAttempt();
			challengeRepository.save(challenge);
			throw new InvalidAuthenticationCodeException();
		}
		challenge.consume(now);
		challengeRepository.save(challenge);
		User user = userRepository.findByEmail(challenge.destination())
				.or(() -> userRepository.findByPhone(challenge.destination()))
				.orElseThrow(InvalidAuthenticationCodeException::new);
		return tokenService.createToken(user, now);
	}

	private AuthenticationChallengeId parseChallengeId(String value) {
		if (value == null || value.isBlank()) {
			throw AuthenticationValidationException.single("challengeId", "Challenge id must not be blank.");
		}
		return new AuthenticationChallengeId(value.trim());
	}
}
