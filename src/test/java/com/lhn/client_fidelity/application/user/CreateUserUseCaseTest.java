package com.lhn.client_fidelity.application.user;

import com.lhn.client_fidelity.domain.user.User;
import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.domain.user.UserType;
import com.lhn.client_fidelity.exception.CommerceAlreadyExistsException;
import com.lhn.client_fidelity.exception.CommerceClientAlreadyExistsException;
import com.lhn.client_fidelity.exception.DuplicateUserPersistenceException;
import com.lhn.client_fidelity.exception.UnknownUserTypeException;
import com.lhn.client_fidelity.exception.UserValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateUserUseCaseTest {

	private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-25T23:30:00Z"), ZoneOffset.UTC);

	private final FakeUserRepository repository = new FakeUserRepository();
	private final CreateUserUseCase useCase = new CreateUserUseCase(repository, CLOCK);

	@Test
	void createsCommerce() {
		CreateUserResult result = useCase.execute(validCommerceCommand());

		assertThat(result.type()).isEqualTo(UserType.COMMERCE);
		assertThat(result.name()).isEqualTo("Padaria Central");
		assertThat(result.email()).isEqualTo("maria@padaria.com");
		assertThat(result.phone()).isEqualTo("5511999999999");
		assertThat(result.governmentIdentifier()).isEqualTo("11222333000181");
		assertThat(result.createdAt()).isEqualTo(CLOCK.instant());
	}

	@Test
	void createsCommerceClient() {
		CreateUserResult result = useCase.execute(validClientCommand());

		assertThat(result.type()).isEqualTo(UserType.COMMERCE_CLIENT);
		assertThat(result.email()).isEqualTo("joao@email.com");
		assertThat(result.phone()).isEqualTo("11988888888");
		assertThat(result.contactName()).isNull();
		assertThat(result.governmentIdentifier()).isNull();
	}

	@Test
	void normalizesCommerceGovernmentIdentifierBeforeDuplicateLookup() {
		useCase.execute(validCommerceCommand());

		assertThat(repository.lastCommerceGovernmentIdentifierLookup).isEqualTo("11222333000181");
	}

	@Test
	void normalizesCommerceClientEmailBeforeDuplicateLookup() {
		useCase.execute(new CreateUserCommand("COMMERCE_CLIENT", "Joao", null, false, "JOAO@EMAIL.COM", "11988888888", null, false));

		assertThat(repository.lastCommerceClientEmailLookup).isEqualTo("joao@email.com");
	}

	@Test
	void rejectsDuplicateCommerce() {
		repository.commerceExists = true;

		assertThatThrownBy(() -> useCase.execute(validCommerceCommand()))
				.isInstanceOf(CommerceAlreadyExistsException.class);
	}

	@Test
	void rejectsDuplicateCommerceClient() {
		repository.commerceClientExists = true;

		assertThatThrownBy(() -> useCase.execute(validClientCommand()))
				.isInstanceOf(CommerceClientAlreadyExistsException.class);
	}

	@Test
	void rejectsMissingType() {
		assertThatThrownBy(() -> useCase.execute(new CreateUserCommand(null, "Name", null, false, "a@b.com", "11999999999", null, false)))
				.isInstanceOf(UnknownUserTypeException.class);
	}

	@Test
	void rejectsUnknownType() {
		assertThatThrownBy(() -> useCase.execute(new CreateUserCommand("ADMIN", "Name", null, false, "a@b.com", "11999999999", null, false)))
				.isInstanceOf(UnknownUserTypeException.class);
	}

	@Test
	void rejectsBlankCommerceName() {
		CreateUserCommand command = new CreateUserCommand(
				"COMMERCE",
				" ",
				"Maria Silva",
				true,
				"maria@padaria.com",
				"11999999999",
				"11.222.333/0001-81",
				true
		);

		assertThatThrownBy(() -> useCase.execute(command))
				.isInstanceOf(UserValidationException.class);
	}

	@Test
	void rejectsBlankCommerceClientName() {
		CreateUserCommand command = new CreateUserCommand(
				"COMMERCE_CLIENT",
				" ",
				null,
				false,
				"joao@email.com",
				"11988888888",
				null,
				false
		);

		assertThatThrownBy(() -> useCase.execute(command))
				.isInstanceOf(UserValidationException.class);
	}

	@ParameterizedTest
	@ValueSource(strings = {"customer@", "@email.com", "customer@localhost", "customer @email.com", "a@b@c.com"})
	void rejectsInvalidEmail(String email) {
		CreateUserCommand command = new CreateUserCommand("COMMERCE_CLIENT", "Joao", null, false, email, "11988888888", null, false);

		assertThatThrownBy(() -> useCase.execute(command))
				.isInstanceOf(UserValidationException.class);
	}

	@ParameterizedTest
	@ValueSource(strings = {"12345", "+55 11 phone", "+55@11999999999", "1234567890123456"})
	void rejectsInvalidPhone(String phone) {
		CreateUserCommand command = new CreateUserCommand("COMMERCE_CLIENT", "Joao", null, false, "joao@email.com", phone, null, false);

		assertThatThrownBy(() -> useCase.execute(command))
				.isInstanceOf(UserValidationException.class);
	}

	@Test
	void rejectsCommerceWithoutContactName() {
		CreateUserCommand command = new CreateUserCommand(
				"COMMERCE",
				"Padaria Central",
				null,
				false,
				"maria@padaria.com",
				"11999999999",
				"11.222.333/0001-81",
				true
		);

		assertThatThrownBy(() -> useCase.execute(command))
				.isInstanceOf(UserValidationException.class);
	}

	@Test
	void rejectsCommerceWithoutGovernmentIdentifier() {
		CreateUserCommand command = new CreateUserCommand(
				"COMMERCE",
				"Padaria Central",
				"Maria Silva",
				true,
				"maria@padaria.com",
				"11999999999",
				null,
				false
		);

		assertThatThrownBy(() -> useCase.execute(command))
				.isInstanceOf(UserValidationException.class);
	}

	@ParameterizedTest
	@ValueSource(strings = {"1234567800019", "123456780001950", "00000000000000", "11.222.333/0001-80"})
	void rejectsInvalidCnpj(String cnpj) {
		CreateUserCommand command = new CreateUserCommand(
				"COMMERCE",
				"Padaria Central",
				"Maria Silva",
				true,
				"maria@padaria.com",
				"11999999999",
				cnpj,
				true
		);

		assertThatThrownBy(() -> useCase.execute(command))
				.isInstanceOf(UserValidationException.class);
	}

	@Test
	void rejectsCommerceClientWithContactName() {
		assertThatThrownBy(() -> useCase.execute(new CreateUserCommand("COMMERCE_CLIENT", "Joao", "", true, "joao@email.com", "11988888888", null, false)))
				.isInstanceOf(UserValidationException.class);
	}

	@Test
	void rejectsCommerceClientWithGovernmentIdentifier() {
		assertThatThrownBy(() -> useCase.execute(new CreateUserCommand("COMMERCE_CLIENT", "Joao", null, false, "joao@email.com", "11988888888", "", true)))
				.isInstanceOf(UserValidationException.class);
	}

	@Test
	void translatesCommerceUniqueRace() {
		repository.throwDuplicateOnSave = true;

		assertThatThrownBy(() -> useCase.execute(validCommerceCommand()))
				.isInstanceOf(CommerceAlreadyExistsException.class);
	}

	@Test
	void translatesCommerceClientUniqueRace() {
		repository.throwDuplicateOnSave = true;

		assertThatThrownBy(() -> useCase.execute(validClientCommand()))
				.isInstanceOf(CommerceClientAlreadyExistsException.class);
	}

	private CreateUserCommand validCommerceCommand() {
		return new CreateUserCommand(
				"COMMERCE",
				"Padaria Central",
				"Maria Silva",
				true,
				"MARIA@PADARIA.COM",
				"+55 11 99999-9999",
				"11.222.333/0001-81",
				true
		);
	}

	private CreateUserCommand validClientCommand() {
		return new CreateUserCommand(
				"COMMERCE_CLIENT",
				"Joao Souza",
				null,
				false,
				"joao@email.com",
				"(11) 98888-8888",
				null,
				false
		);
	}

	private static class FakeUserRepository implements UserRepository {

		private boolean commerceExists;
		private boolean commerceClientExists;
		private boolean throwDuplicateOnSave;
		private String lastCommerceGovernmentIdentifierLookup;
		private String lastCommerceClientEmailLookup;

		@Override
		public boolean existsCommerceByGovernmentIdentifier(String governmentIdentifier) {
			lastCommerceGovernmentIdentifierLookup = governmentIdentifier;
			return commerceExists;
		}

		@Override
		public boolean existsCommerceClientByEmail(String email) {
			lastCommerceClientEmailLookup = email;
			return commerceClientExists;
		}

		@Override
		public Optional<User> findById(UserId id) {
			return Optional.empty();
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
			if (throwDuplicateOnSave) {
				throw new DuplicateUserPersistenceException();
			}
			return user;
		}
	}
}
