package com.lhn.client_fidelity.domain.campaign;

import tools.jackson.databind.JsonNode;
import com.lhn.client_fidelity.domain.user.UserId;

import java.time.Instant;
import java.time.LocalDate;

public record Campaign(
		CampaignId id,
		UserId commerceId,
		String name,
		int points,
		LocalDate startDate,
		LocalDate expirationDate,
		Instant updatedAt,
		JsonNode template
) {
}

