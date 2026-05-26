package com.lhn.client_fidelity.application.campaign;

import tools.jackson.databind.JsonNode;
import com.lhn.client_fidelity.exception.CampaignValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

final class CampaignValidationSupport {

	private CampaignValidationSupport() {
	}

	static String requireName(String value) {
		if (value == null || value.trim().isEmpty()) {
			throw CampaignValidationException.single("name", "Name must not be blank.");
		}
		return value.trim();
	}

	static int requirePoints(Integer value) {
		if (value == null) {
			throw CampaignValidationException.single("points", "Points must be provided.");
		}
		if (value < 0) {
			throw CampaignValidationException.single("points", "Points must be zero or greater.");
		}
		return value;
	}

	static LocalDate requireDate(String field, String value) {
		if (value == null || value.isBlank()) {
			throw CampaignValidationException.single(field, field + " must be provided.");
		}
		try {
			return LocalDate.parse(value.trim());
		}
		catch (DateTimeParseException exception) {
			throw CampaignValidationException.single(field, field + " must be a valid ISO-8601 date.");
		}
	}

	static JsonNode requireTemplate(JsonNode template) {
		if (template == null || template.isNull() || !template.isObject()) {
			throw CampaignValidationException.single("template", "Template must be a JSON object.");
		}
		return template.deepCopy();
	}

	static void validateDateOrder(LocalDate startDate, LocalDate expirationDate) {
		if (expirationDate.isBefore(startDate)) {
			throw CampaignValidationException.single(
					"expirationDate",
					"Expiration date must be equal to or after start date."
			);
		}
	}
}

