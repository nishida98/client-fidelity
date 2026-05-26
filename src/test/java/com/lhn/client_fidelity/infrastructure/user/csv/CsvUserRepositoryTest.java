package com.lhn.client_fidelity.infrastructure.user.csv;

import com.lhn.client_fidelity.domain.user.Commerce;
import com.lhn.client_fidelity.domain.user.CommerceClient;
import com.lhn.client_fidelity.domain.user.Email;
import com.lhn.client_fidelity.domain.user.GovernmentIdentifier;
import com.lhn.client_fidelity.domain.user.Phone;
import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.exception.DuplicateUserPersistenceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvUserRepositoryTest {

	@TempDir
	private Path tempDir;

	@Test
	void savesCommerceAndFindsItByGovernmentIdentifier() {
		CsvUserRepository repository = new CsvUserRepository(tempDir.resolve("users.csv"));

		repository.save(commerce("usr_1", "11.222.333/0001-81"));

		assertThat(repository.existsCommerceByGovernmentIdentifier("11222333000181")).isTrue();
	}

	@Test
	void savesCommerceClientAndFindsItByEmail() {
		CsvUserRepository repository = new CsvUserRepository(tempDir.resolve("users.csv"));

		repository.save(client("usr_1", "client@email.com"));

		assertThat(repository.existsCommerceClientByEmail("client@email.com")).isTrue();
	}

	@Test
	void rejectsDuplicateCommerceByGovernmentIdentifier() {
		CsvUserRepository repository = new CsvUserRepository(tempDir.resolve("users.csv"));
		repository.save(commerce("usr_1", "11.222.333/0001-81"));

		assertThatThrownBy(() -> repository.save(commerce("usr_2", "11.222.333/0001-81")))
				.isInstanceOf(DuplicateUserPersistenceException.class);
	}

	@Test
	void rejectsDuplicateCommerceClientByEmail() {
		CsvUserRepository repository = new CsvUserRepository(tempDir.resolve("users.csv"));
		repository.save(client("usr_1", "client@email.com"));

		assertThatThrownBy(() -> repository.save(client("usr_2", "client@email.com")))
				.isInstanceOf(DuplicateUserPersistenceException.class);
	}

	private Commerce commerce(String id, String cnpj) {
		return new Commerce(
				new UserId(id),
				"Padaria Central",
				"Maria Silva",
				new Email("commerce@email.com"),
				new Phone("11999999999"),
				new GovernmentIdentifier(cnpj),
				Instant.parse("2026-05-25T23:30:00Z")
		);
	}

	private CommerceClient client(String id, String email) {
		return new CommerceClient(
				new UserId(id),
				"Joao Souza",
				new Email(email),
				new Phone("11988888888"),
				Instant.parse("2026-05-25T23:30:00Z")
		);
	}
}
