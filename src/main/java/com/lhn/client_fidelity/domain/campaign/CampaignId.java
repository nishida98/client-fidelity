package com.lhn.client_fidelity.domain.campaign;

import java.util.UUID;

public record CampaignId(String value) {

	public CampaignId {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Campaign id must not be blank");
		}
	}

	public static CampaignId newId() {
		return new CampaignId("cam_" + UUID.randomUUID().toString().replace("-", ""));
	}
}
