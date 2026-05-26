package com.lhn.client_fidelity.application.campaign;

import tools.jackson.databind.JsonNode;
import com.lhn.client_fidelity.domain.campaign.Campaign;
import com.lhn.client_fidelity.domain.campaign.CampaignId;
import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.exception.CampaignCreationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

@Service
public class CreateCampaignUseCase {

	private final CampaignRepository campaignRepository;
	private final Clock clock;

	public CreateCampaignUseCase(CampaignRepository campaignRepository, Clock clock) {
		this.campaignRepository = campaignRepository;
		this.clock = clock;
	}

	public CreateCampaignResult execute(CreateCampaignCommand command) {
		String normalizedName = CampaignValidationSupport.requireName(command.name());
		int normalizedPoints = CampaignValidationSupport.requirePoints(command.points());
		LocalDate normalizedStartDate = CampaignValidationSupport.requireDate("startDate", command.startDate());
		LocalDate normalizedExpirationDate = CampaignValidationSupport.requireDate(
				"expirationDate",
				command.expirationDate()
		);
		CampaignValidationSupport.validateDateOrder(normalizedStartDate, normalizedExpirationDate);
		JsonNode normalizedTemplate = CampaignValidationSupport.requireTemplate(command.template());
		Campaign campaign = new Campaign(
				CampaignId.newId(),
				new UserId(command.commerceId()),
				normalizedName,
				normalizedPoints,
				normalizedStartDate,
				normalizedExpirationDate,
				clock.instant(),
				normalizedTemplate
		);
		try {
			return CreateCampaignResult.from(campaignRepository.save(campaign));
		}
		catch (RuntimeException exception) {
			throw new CampaignCreationException(exception);
		}
	}
}

