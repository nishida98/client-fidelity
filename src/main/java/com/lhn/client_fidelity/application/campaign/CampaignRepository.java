package com.lhn.client_fidelity.application.campaign;

import com.lhn.client_fidelity.domain.campaign.Campaign;
import com.lhn.client_fidelity.domain.campaign.CampaignId;
import com.lhn.client_fidelity.domain.user.UserId;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository {

	Campaign save(Campaign campaign);

	Optional<Campaign> findByIdAndCommerceId(CampaignId campaignId, UserId commerceId);

	List<Campaign> findAllByCommerceId(UserId commerceId);

	boolean deleteByIdAndCommerceId(CampaignId campaignId, UserId commerceId);
}
