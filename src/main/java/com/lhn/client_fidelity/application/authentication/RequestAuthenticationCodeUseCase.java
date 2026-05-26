package com.lhn.client_fidelity.application.authentication;

import com.lhn.client_fidelity.application.user.UserRepository;
import com.lhn.client_fidelity.domain.authentication.AuthenticationChallenge;
import com.lhn.client_fidelity.domain.authentication.AuthenticationCode;
import com.lhn.client_fidelity.domain.authentication.AuthenticationMethod;
import com.lhn.client_fidelity.domain.user.Email;
import com.lhn.client_fidelity.domain.user.Phone;
import com.lhn.client_fidelity.domain.user.User;
import com.lhn.client_fidelity.exception.AuthenticationCodeAlreadySentException;
import com.lhn.client_fidelity.exception.AuthenticationDeliveryFailedException;
import com.lhn.client_fidelity.exception.PhoneDeliveryNotConfiguredException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class RequestAuthenticationCodeUseCase {

	private final UserRepository userRepository;
	private final AuthenticationChallengeRepository challengeRepository;
	private final List<AuthenticationCodeDelivery> deliveries;
	private final AuthenticationProperties properties;
	private final Clock clock;

	public RequestAuthenticationCodeUseCase(
			UserRepository userRepository,
			AuthenticationChallengeRepository challengeRepository,
			List<AuthenticationCodeDelivery> deliveries,
			AuthenticationProperties properties,
			Clock clock
	) {
		this.userRepository = userRepository;
		this.challengeRepository = challengeRepository;
		this.deliveries = deliveries;
		this.properties = properties;
		this.clock = clock;
	}

	public RequestAuthenticationCodeResult execute(RequestAuthenticationCodeCommand command) {
		AuthenticationMethod method = AuthenticationMethod.parse(command.method());
		String destination = normalizeIdentifier(method, command.identifier());
		Optional<User> user = findUser(method, destination);
		if (user.isEmpty()) {
			return RequestAuthenticationCodeResult.acceptedWithoutChallenge(method);
		}

		Instant now = clock.instant();
		challengeRepository.findActiveByUserIdAndMethod(user.get().id(), method, now)
				.ifPresent(challenge -> {
					throw new AuthenticationCodeAlreadySentException(challenge.expiresAt());
				});

		AuthenticationCode code = AuthenticationCode.generate();
		AuthenticationChallenge challenge = AuthenticationChallenge.create(
				user.get().id(),
				method,
				destination,
				code,
				now,
				properties.codeExpiration()
		);
		AuthenticationChallenge savedChallenge = challengeRepository.save(challenge);
		sendCode(method, destination, code);
		return new RequestAuthenticationCodeResult(
				savedChallenge.id().value(),
				savedChallenge.method(),
				savedChallenge.expiresAt()
		);
	}

	private String normalizeIdentifier(AuthenticationMethod method, String identifier) {
		if (method == AuthenticationMethod.EMAIL) {
			return new Email(identifier).value();
		}
		return new Phone(identifier).value();
	}

	private Optional<User> findUser(AuthenticationMethod method, String destination) {
		if (method == AuthenticationMethod.EMAIL) {
			return userRepository.findByEmail(destination);
		}
		return userRepository.findByPhone(destination);
	}

	private void sendCode(AuthenticationMethod method, String destination, AuthenticationCode code) {
		String provider = properties.providerFor(method);
		AuthenticationCodeDelivery delivery = deliveries.stream()
				.filter(candidate -> candidate.supports(method, provider))
				.findFirst()
				.orElseThrow(() -> new PhoneDeliveryNotConfiguredException());
		try {
			delivery.send(destination, code.value());
		}
		catch (PhoneDeliveryNotConfiguredException exception) {
			throw exception;
		}
		catch (RuntimeException exception) {
			throw new AuthenticationDeliveryFailedException(exception);
		}
	}
}
