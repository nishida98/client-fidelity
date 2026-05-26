package com.lhn.client_fidelity.application.campaign;

import tools.jackson.databind.JsonNode;

public record PatchCampaignCommand(
		String campaignId,
		String commerceId,
		String name,
		boolean namePresent,
		Integer points,
		boolean pointsPresent,
		String startDate,
		boolean startDatePresent,
		String expirationDate,
		boolean expirationDatePresent,
		JsonNode template,
		boolean templatePresent
) {
}

