package com.lhn.client_fidelity.interfaces.rest.campaign;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.JsonNode;
import com.lhn.client_fidelity.application.campaign.ReplaceCampaignCommand;
import com.lhn.client_fidelity.domain.user.FieldValidationError;
import com.lhn.client_fidelity.exception.CampaignValidationException;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public class ReplaceCampaignRequest {

	private String name;
	private Integer points;
	private String startDate;
	private String expirationDate;
	private JsonNode template;
	private final List<String> unknownFields = new ArrayList<>();

	public void setName(String name) {
		this.name = name;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public void setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
	}

	public void setTemplate(JsonNode template) {
		this.template = template;
	}

	@JsonAnySetter
	public void setUnknownField(String field, Object value) {
		unknownFields.add(field);
	}

	ReplaceCampaignCommand toCommand(String campaignId, String commerceId) {
		if (!unknownFields.isEmpty()) {
			throw new CampaignValidationException(unknownFields.stream()
					.map(field -> new FieldValidationError(field, "Field is not allowed."))
					.toList());
		}
		return new ReplaceCampaignCommand(campaignId, commerceId, name, points, startDate, expirationDate, template);
	}
}

