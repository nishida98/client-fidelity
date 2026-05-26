package com.lhn.client_fidelity.application.campaign;

import tools.jackson.databind.JsonNode;

public record CreateCampaignCommand(
		String commerceId,
		String name,
		Integer points,
		String startDate,
		String expirationDate,
		JsonNode template
) {
}

