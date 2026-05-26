package com.lhn.client_fidelity.application.campaign;

import com.lhn.client_fidelity.domain.campaign.CampaignId;
import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.exception.CampaignNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetCampaignByIdUseCase {

	private final CampaignRepository campaignRepository;

	public GetCampaignByIdUseCase(CampaignRepository campaignRepository) {
		this.campaignRepository = campaignRepository;
	}

	public CreateCampaignResult execute(String campaignId, String commerceId) {
		return campaignRepository.findByIdAndCommerceId(new CampaignId(campaignId), new UserId(commerceId))
				.map(CreateCampaignResult::from)
				.orElseThrow(CampaignNotFoundException::new);
	}
}
