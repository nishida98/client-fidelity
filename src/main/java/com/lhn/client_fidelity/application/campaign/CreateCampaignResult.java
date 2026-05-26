package com.lhn.client_fidelity.application.campaign;

import tools.jackson.databind.JsonNode;
import com.lhn.client_fidelity.domain.campaign.Campaign;

import java.time.Instant;
import java.time.LocalDate;

public record CreateCampaignResult(
		String id,
		String name,
		int points,
		LocalDate startDate,
		LocalDate expirationDate,
		Instant updatedAt,
		JsonNode template
) {

	public static CreateCampaignResult from(Campaign campaign) {
		return new CreateCampaignResult(
				campaign.id().value(),
				campaign.name(),
				campaign.points(),
				campaign.startDate(),
				campaign.expirationDate(),
				campaign.updatedAt(),
				campaign.template()
		);
	}
}

