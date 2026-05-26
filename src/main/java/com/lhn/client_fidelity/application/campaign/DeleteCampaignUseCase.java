package com.lhn.client_fidelity.application.campaign;

import com.lhn.client_fidelity.domain.campaign.CampaignId;
import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.exception.CampaignDeletionException;
import com.lhn.client_fidelity.exception.CampaignNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DeleteCampaignUseCase {

	private final CampaignRepository campaignRepository;

	public DeleteCampaignUseCase(CampaignRepository campaignRepository) {
		this.campaignRepository = campaignRepository;
	}

	public void execute(String campaignId, String commerceId) {
		try {
			boolean deleted = campaignRepository.deleteByIdAndCommerceId(
					new CampaignId(campaignId),
					new UserId(commerceId)
			);
			if (!deleted) {
				throw new CampaignNotFoundException();
			}
		}
		catch (CampaignNotFoundException exception) {
			throw exception;
		}
		catch (RuntimeException exception) {
			throw new CampaignDeletionException(exception);
		}
	}
}
