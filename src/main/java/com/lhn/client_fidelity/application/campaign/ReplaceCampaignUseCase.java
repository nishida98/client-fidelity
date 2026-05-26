package com.lhn.client_fidelity.application.campaign;

import tools.jackson.databind.JsonNode;
import com.lhn.client_fidelity.domain.campaign.Campaign;
import com.lhn.client_fidelity.domain.campaign.CampaignId;
import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.exception.CampaignNotFoundException;
import com.lhn.client_fidelity.exception.CampaignUpdateException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

@Service
public class ReplaceCampaignUseCase {

	private final CampaignRepository campaignRepository;
	private final Clock clock;

	public ReplaceCampaignUseCase(CampaignRepository campaignRepository, Clock clock) {
		this.campaignRepository = campaignRepository;
		this.clock = clock;
	}

	public CreateCampaignResult execute(ReplaceCampaignCommand command) {
		Campaign existing = campaignRepository.findByIdAndCommerceId(
						new CampaignId(command.campaignId()),
						new UserId(command.commerceId())
				)
				.orElseThrow(CampaignNotFoundException::new);
		String normalizedName = CampaignValidationSupport.requireName(command.name());
		int normalizedPoints = CampaignValidationSupport.requirePoints(command.points());
		LocalDate normalizedStartDate = CampaignValidationSupport.requireDate("startDate", command.startDate());
		LocalDate normalizedExpirationDate = CampaignValidationSupport.requireDate(
				"expirationDate",
				command.expirationDate()
		);
		CampaignValidationSupport.validateDateOrder(normalizedStartDate, normalizedExpirationDate);
		JsonNode normalizedTemplate = CampaignValidationSupport.requireTemplate(command.template());
		Campaign updatedCampaign = new Campaign(
				existing.id(),
				existing.commerceId(),
				normalizedName,
				normalizedPoints,
				normalizedStartDate,
				normalizedExpirationDate,
				clock.instant(),
				normalizedTemplate
		);
		try {
			return CreateCampaignResult.from(campaignRepository.save(updatedCampaign));
		}
		catch (RuntimeException exception) {
			throw new CampaignUpdateException(exception);
		}
	}
}

