package com.lhn.client_fidelity.application.campaign;

import com.lhn.client_fidelity.domain.user.UserId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListCommerceCampaignsUseCase {

	private final CampaignRepository campaignRepository;

	public ListCommerceCampaignsUseCase(CampaignRepository campaignRepository) {
		this.campaignRepository = campaignRepository;
	}

	public List<CreateCampaignResult> execute(String commerceId) {
		return campaignRepository.findAllByCommerceId(new UserId(commerceId)).stream()
				.map(CreateCampaignResult::from)
				.toList();
	}
}
