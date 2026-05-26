package com.lhn.client_fidelity.interfaces.rest.campaign;

import tools.jackson.databind.JsonNode;
import com.lhn.client_fidelity.application.campaign.CreateCampaignResult;

import java.time.Instant;
import java.time.LocalDate;

public record CampaignResponse(
		String id,
		String name,
		int points,
		LocalDate startDate,
		LocalDate expirationDate,
		Instant updatedAt,
		JsonNode template
) {

	static CampaignResponse from(CreateCampaignResult result) {
		return new CampaignResponse(
				result.id(),
				result.name(),
				result.points(),
				result.startDate(),
				result.expirationDate(),
				result.updatedAt(),
				result.template()
		);
	}
}

