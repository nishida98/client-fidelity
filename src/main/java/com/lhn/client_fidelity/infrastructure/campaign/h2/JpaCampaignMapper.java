package com.lhn.client_fidelity.infrastructure.campaign.h2;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.lhn.client_fidelity.domain.campaign.Campaign;
import com.lhn.client_fidelity.domain.campaign.CampaignId;
import com.lhn.client_fidelity.domain.user.UserId;

final class JpaCampaignMapper {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private JpaCampaignMapper() {
	}

	static CampaignEntity toEntity(Campaign campaign) {
		return new CampaignEntity(
				campaign.id().value(),
				campaign.commerceId().value(),
				campaign.name(),
				campaign.points(),
				campaign.startDate(),
				campaign.expirationDate(),
				campaign.updatedAt(),
				writeTemplate(campaign.template())
		);
	}

	static Campaign toDomain(CampaignEntity entity) {
		return new Campaign(
				new CampaignId(entity.id()),
				new UserId(entity.commerceId()),
				entity.name(),
				entity.points(),
				entity.startDate(),
				entity.expirationDate(),
				entity.updatedAt(),
				readTemplate(entity.templatePayload())
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

	private static JsonNode readTemplate(String templatePayload) {
		try {
			return OBJECT_MAPPER.readTree(templatePayload);
		}
		catch (Exception exception) {
			throw new IllegalStateException("Could not deserialize campaign template.", exception);
		}
	}
}

