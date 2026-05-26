package com.lhn.client_fidelity.application.user;

import com.lhn.client_fidelity.domain.user.Commerce;
import com.lhn.client_fidelity.domain.user.CommerceClient;
import com.lhn.client_fidelity.domain.user.Email;
import com.lhn.client_fidelity.domain.user.GovernmentIdentifier;
import com.lhn.client_fidelity.domain.user.Phone;
import com.lhn.client_fidelity.domain.user.User;
import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.domain.user.UserType;
import com.lhn.client_fidelity.exception.CommerceAlreadyExistsException;
import com.lhn.client_fidelity.exception.CommerceClientAlreadyExistsException;
import com.lhn.client_fidelity.exception.DuplicateUserPersistenceException;
import com.lhn.client_fidelity.exception.UnknownUserTypeException;
import com.lhn.client_fidelity.exception.UserCreationException;
import com.lhn.client_fidelity.exception.UserValidationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Locale;

@Service
public class CreateUserUseCase {

	private final UserRepository userRepository;
	private final Clock clock;

	public CreateUserUseCase(UserRepository userRepository, Clock clock) {
		this.userRepository = userRepository;
		this.clock = clock;
	}

	public CreateUserResult execute(CreateUserCommand command) {
		UserType type = parseType(command.type());
		User user = switch (type) {
			case COMMERCE -> createCommerce(command);
			case COMMERCE_CLIENT -> createCommerceClient(command);
		};
		try {
			return CreateUserResult.from(userRepository.save(user));
		}
		catch (DuplicateUserPersistenceException exception) {
			throw duplicateExceptionFor(type);
		}
		catch (RuntimeException exception) {
			throw new UserCreationException(exception);
		}
	}

	private UserType parseType(String rawType) {
		if (rawType == null || rawType.trim().isEmpty()) {
			throw new UnknownUserTypeException();
		}
		try {
			return UserType.valueOf(rawType.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException exception) {
			throw new UnknownUserTypeException();
		}
	}

	private Commerce createCommerce(CreateUserCommand command) {
		Email email = new Email(command.email());
		Phone phone = new Phone(command.phone());
		GovernmentIdentifier governmentIdentifier = new GovernmentIdentifier(command.governmentIdentifier());
		Commerce commerce = new Commerce(
				UserId.newId(),
				command.name(),
				command.contactName(),
				email,
				phone,
				governmentIdentifier,
				clock.instant()
		);
		if (userRepository.existsCommerceByGovernmentIdentifier(governmentIdentifier.value())) {
			throw new CommerceAlreadyExistsException();
		}
		return commerce;
	}

	private CommerceClient createCommerceClient(CreateUserCommand command) {
		rejectCommerceOnlyFields(command);
		Email email = new Email(command.email());
		Phone phone = new Phone(command.phone());
		CommerceClient client = new CommerceClient(
				UserId.newId(),
				command.name(),
				email,
				phone,
				clock.instant()
		);
		if (userRepository.existsCommerceClientByEmail(email.value())) {
			throw new CommerceClientAlreadyExistsException();
		}
		return client;
	}

	private void rejectCommerceOnlyFields(CreateUserCommand command) {
		if (command.contactNamePresent()) {
			throw UserValidationException.single("contactName", "Contact name is not allowed for commerce clients.");
		}
		if (command.governmentIdentifierPresent()) {
			throw UserValidationException.single(
					"governmentIdentifier",
					"Government identifier is not allowed for commerce clients."
			);
		}
	}

	private RuntimeException duplicateExceptionFor(UserType type) {
		if (type == UserType.COMMERCE) {
			return new CommerceAlreadyExistsException();
		}
		return new CommerceClientAlreadyExistsException();
	}

}
