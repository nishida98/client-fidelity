package com.lhn.client_fidelity.interfaces.rest.campaign;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.JsonNode;
import com.lhn.client_fidelity.application.campaign.PatchCampaignCommand;
import com.lhn.client_fidelity.domain.user.FieldValidationError;
import com.lhn.client_fidelity.exception.CampaignValidationException;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public class PatchCampaignRequest {

	private String name;
	private boolean namePresent;
	private Integer points;
	private boolean pointsPresent;
	private String startDate;
	private boolean startDatePresent;
	private String expirationDate;
	private boolean expirationDatePresent;
	private JsonNode template;
	private boolean templatePresent;
	private final List<String> unknownFields = new ArrayList<>();

	public void setName(String name) {
		this.name = name;
		this.namePresent = true;
	}

	public void setPoints(Integer points) {
		this.points = points;
		this.pointsPresent = true;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
		this.startDatePresent = true;
	}

	public void setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
		this.expirationDatePresent = true;
	}

	public void setTemplate(JsonNode template) {
		this.template = template;
		this.templatePresent = true;
	}

	@JsonAnySetter
	public void setUnknownField(String field, Object value) {
		unknownFields.add(field);
	}

	PatchCampaignCommand toCommand(String campaignId, String commerceId) {
		if (!unknownFields.isEmpty()) {
			throw new CampaignValidationException(unknownFields.stream()
					.map(field -> new FieldValidationError(field, "Field is not allowed."))
					.toList());
		}
		return new PatchCampaignCommand(
				campaignId,
				commerceId,
				name,
				namePresent,
				points,
				pointsPresent,
				startDate,
				startDatePresent,
				expirationDate,
				expirationDatePresent,
				template,
				templatePresent
		);
	}
}

