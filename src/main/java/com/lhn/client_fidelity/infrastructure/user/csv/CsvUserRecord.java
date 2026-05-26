package com.lhn.client_fidelity.infrastructure.user.csv;

import com.lhn.client_fidelity.domain.user.Commerce;
import com.lhn.client_fidelity.domain.user.CommerceClient;
import com.lhn.client_fidelity.domain.user.Email;
import com.lhn.client_fidelity.domain.user.GovernmentIdentifier;
import com.lhn.client_fidelity.domain.user.Phone;
import com.lhn.client_fidelity.domain.user.User;
import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.domain.user.UserType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

record CsvUserRecord(
		String id,
		String type,
		String name,
		String contactName,
		String email,
		String phone,
		String governmentIdentifier,
		String createdAt
) {

	static CsvUserRecord from(User user) {
		if (user instanceof Commerce commerce) {
			return new CsvUserRecord(
					commerce.id().value(),
					commerce.type().name(),
					commerce.name(),
					commerce.contactName(),
					commerce.email().value(),
					commerce.phone().value(),
					commerce.governmentIdentifier().value(),
					commerce.createdAt().toString()
			);
		}
		CommerceClient client = (CommerceClient) user;
		return new CsvUserRecord(
				client.id().value(),
				client.type().name(),
				client.name(),
				"",
				client.email().value(),
				client.phone().value(),
				"",
				client.createdAt().toString()
		);
	}

	static CsvUserRecord fromLine(String line) {
		List<String> fields = parse(line);
		if (fields.size() != 8) {
			throw new IllegalStateException("Invalid user CSV record.");
		}
		return new CsvUserRecord(
				fields.get(0),
				fields.get(1),
				fields.get(2),
				fields.get(3),
				fields.get(4),
				fields.get(5),
				fields.get(6),
				fields.get(7)
		);
	}

	String toLine() {
		return String.join(
				",",
				escape(id),
				escape(type),
				escape(name),
				escape(contactName),
				escape(email),
				escape(phone),
				escape(governmentIdentifier),
				escape(createdAt)
		);
	}

	User toDomain() {
		if (UserType.COMMERCE.name().equals(type)) {
			return new Commerce(
					new UserId(id),
					name,
					contactName,
					new Email(email),
					new Phone(phone),
					new GovernmentIdentifier(governmentIdentifier),
					Instant.parse(createdAt)
			);
		}
		return new CommerceClient(
				new UserId(id),
				name,
				new Email(email),
				new Phone(phone),
				Instant.parse(createdAt)
		);
	}

	private static String escape(String value) {
		String safeValue = value == null ? "" : value;
		if (safeValue.contains(",") || safeValue.contains("\"") || safeValue.contains("\n")) {
			return "\"" + safeValue.replace("\"", "\"\"") + "\"";
		}
		return safeValue;
	}

	private static List<String> parse(String line) {
		List<String> fields = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean quoted = false;
		for (int index = 0; index < line.length(); index++) {
			char character = line.charAt(index);
			if (character == '"') {
				if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
					current.append('"');
					index++;
				}
				else {
					quoted = !quoted;
				}
			}
			else if (character == ',' && !quoted) {
				fields.add(current.toString());
				current.setLength(0);
			}
			else {
				current.append(character);
			}
		}
		fields.add(current.toString());
		return fields;
	}
}
