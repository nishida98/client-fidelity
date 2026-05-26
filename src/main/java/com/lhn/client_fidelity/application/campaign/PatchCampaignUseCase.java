package com.lhn.client_fidelity.application.campaign;

import tools.jackson.databind.JsonNode;
import com.lhn.client_fidelity.domain.campaign.Campaign;
import com.lhn.client_fidelity.domain.campaign.CampaignId;
import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.exception.CampaignNotFoundException;
import com.lhn.client_fidelity.exception.CampaignUpdateException;
import com.lhn.client_fidelity.exception.CampaignValidationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

@Service
public class PatchCampaignUseCase {

	private final CampaignRepository campaignRepository;
	private final Clock clock;

	public PatchCampaignUseCase(CampaignRepository campaignRepository, Clock clock) {
		this.campaignRepository = campaignRepository;
		this.clock = clock;
	}

	public CreateCampaignResult execute(PatchCampaignCommand command) {
		if (!hasChanges(command)) {
			throw CampaignValidationException.single("request", "At least one field must be provided.");
		}
		Campaign existing = campaignRepository.findByIdAndCommerceId(
						new CampaignId(command.campaignId()),
						new UserId(command.commerceId())
				)
				.orElseThrow(CampaignNotFoundException::new);

		String normalizedName = command.namePresent()
				? CampaignValidationSupport.requireName(command.name())
				: existing.name();
		int normalizedPoints = command.pointsPresent()
				? CampaignValidationSupport.requirePoints(command.points())
				: existing.points();
		LocalDate normalizedStartDate = command.startDatePresent()
				? CampaignValidationSupport.requireDate("startDate", command.startDate())
				: existing.startDate();
		LocalDate normalizedExpirationDate = command.expirationDatePresent()
				? CampaignValidationSupport.requireDate("expirationDate", command.expirationDate())
				: existing.expirationDate();
		CampaignValidationSupport.validateDateOrder(normalizedStartDate, normalizedExpirationDate);
		JsonNode normalizedTemplate = command.templatePresent()
				? CampaignValidationSupport.requireTemplate(command.template())
				: existing.template().deepCopy();
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

	private boolean hasChanges(PatchCampaignCommand command) {
		return command.namePresent()
				|| command.pointsPresent()
				|| command.startDatePresent()
				|| command.expirationDatePresent()
				|| command.templatePresent();
	}
}

