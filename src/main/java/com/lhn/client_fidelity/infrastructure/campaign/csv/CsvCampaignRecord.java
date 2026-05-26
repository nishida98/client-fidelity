package com.lhn.client_fidelity.infrastructure.campaign.csv;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.lhn.client_fidelity.domain.campaign.Campaign;
import com.lhn.client_fidelity.domain.campaign.CampaignId;
import com.lhn.client_fidelity.domain.user.UserId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

record CsvCampaignRecord(
		String id,
		String commerceId,
		String name,
		String points,
		String startDate,
		String expirationDate,
		String updatedAt,
		String templatePayload
) {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	static CsvCampaignRecord from(Campaign campaign) {
		return new CsvCampaignRecord(
				campaign.id().value(),
				campaign.commerceId().value(),
				campaign.name(),
				Integer.toString(campaign.points()),
				campaign.startDate().toString(),
				campaign.expirationDate().toString(),
				campaign.updatedAt().toString(),
				writeTemplate(campaign.template())
		);
	}

	static CsvCampaignRecord fromLine(String line) {
		List<String> fields = parse(line);
		if (fields.size() != 8) {
			throw new IllegalStateException("Invalid campaign CSV record.");
		}
		return new CsvCampaignRecord(
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
				escape(commerceId),
				escape(name),
				escape(points),
				escape(startDate),
				escape(expirationDate),
				escape(updatedAt),
				escape(templatePayload)
		);
	}

	Campaign toDomain() {
		return new Campaign(
				new CampaignId(id),
				new UserId(commerceId),
				name,
				Integer.parseInt(points),
				LocalDate.parse(startDate),
				LocalDate.parse(expirationDate),
				Instant.parse(updatedAt),
				readTemplate(templatePayload)
		);
	}

	private static String writeTemplate(JsonNode template) {
		try {
			return OBJECT_MAPPER.writeValueAsString(template);
		}
		catch (Exception exception) {
			throw new IllegalStateException("Could not serialize campaign template.", exception);
		}
	}

	private static JsonNode readTemplate(String value) {
		try {
			return OBJECT_MAPPER.readTree(value);
		}
		catch (Exception exception) {
			throw new IllegalStateException("Could not deserialize campaign template.", exception);
		}
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

