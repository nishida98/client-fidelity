package com.lhn.client_fidelity.infrastructure.user.csv;

import com.lhn.client_fidelity.application.user.UserRepository;
import com.lhn.client_fidelity.domain.user.Commerce;
import com.lhn.client_fidelity.domain.user.CommerceClient;
import com.lhn.client_fidelity.domain.user.User;
import com.lhn.client_fidelity.exception.DuplicateUserPersistenceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "client-fidelity.persistence.type", havingValue = "csv")
public class CsvUserRepository implements UserRepository {

	private static final String HEADER = "id,type,name,contact_name,email,phone,government_identifier,created_at";

	private final Path path;

	public CsvUserRepository(@Value("${client-fidelity.persistence.csv.path}") String path) {
		this.path = Path.of(path);
	}

	public CsvUserRepository(Path path) {
		this.path = path;
	}

	@Override
	public synchronized boolean existsCommerceByGovernmentIdentifier(String governmentIdentifier) {
		return records().stream()
				.anyMatch(record -> "COMMERCE".equals(record.type())
						&& governmentIdentifier.equals(record.governmentIdentifier()));
	}

	@Override
	public synchronized boolean existsCommerceClientByEmail(String email) {
		return records().stream()
				.anyMatch(record -> "COMMERCE_CLIENT".equals(record.type()) && email.equals(record.email()));
	}

	@Override
	public synchronized Optional<User> findByEmail(String email) {
		return records().stream()
				.filter(record -> email.equals(record.email()))
				.findFirst()
				.map(CsvUserRecord::toDomain);
	}

	@Override
	public synchronized Optional<User> findByPhone(String phone) {
		return records().stream()
				.filter(record -> phone.equals(record.phone()))
				.findFirst()
				.map(CsvUserRecord::toDomain);
	}

	@Override
	public synchronized User save(User user) {
		if (isDuplicate(user)) {
			throw new DuplicateUserPersistenceException();
		}
		append(user);
		return user;
	}

	private boolean isDuplicate(User user) {
		if (user instanceof Commerce commerce) {
			return existsCommerceByGovernmentIdentifier(commerce.governmentIdentifier().value());
		}
		CommerceClient client = (CommerceClient) user;
		return existsCommerceClientByEmail(client.email().value());
	}

	private void append(User user) {
		try {
			ensureFileExists();
			Files.writeString(
					path,
					CsvUserRecord.from(user).toLine() + System.lineSeparator(),
					StandardCharsets.UTF_8,
					java.nio.file.StandardOpenOption.APPEND
			);
		}
		catch (IOException exception) {
			throw new IllegalStateException("Could not write user CSV file.", exception);
		}
	}

	private List<CsvUserRecord> records() {
		try {
			ensureFileExists();
			List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
			List<CsvUserRecord> records = new ArrayList<>();
			for (int index = 1; index < lines.size(); index++) {
				if (!lines.get(index).isBlank()) {
					records.add(CsvUserRecord.fromLine(lines.get(index)));
				}
			}
			return records;
		}
		catch (IOException exception) {
			throw new IllegalStateException("Could not read user CSV file.", exception);
		}
	}

	private void ensureFileExists() throws IOException {
		Path parent = path.getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
		if (Files.notExists(path)) {
			Files.writeString(path, HEADER + System.lineSeparator(), StandardCharsets.UTF_8);
		}
	}
}
