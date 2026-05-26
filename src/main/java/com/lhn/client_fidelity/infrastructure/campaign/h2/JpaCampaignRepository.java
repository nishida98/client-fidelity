package com.lhn.client_fidelity.infrastructure.campaign.h2;

import com.lhn.client_fidelity.application.campaign.CampaignRepository;
import com.lhn.client_fidelity.domain.campaign.Campaign;
import com.lhn.client_fidelity.domain.campaign.CampaignId;
import com.lhn.client_fidelity.domain.user.UserId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "client-fidelity.persistence.type", havingValue = "h2", matchIfMissing = true)
class JpaCampaignRepository implements CampaignRepository {

	private final JpaCampaignCrudRepository repository;

	JpaCampaignRepository(JpaCampaignCrudRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public Campaign save(Campaign campaign) {
		return JpaCampaignMapper.toDomain(repository.saveAndFlush(JpaCampaignMapper.toEntity(campaign)));
	}

	@Override
	public Optional<Campaign> findByIdAndCommerceId(CampaignId campaignId, UserId commerceId) {
		return repository.findByIdAndCommerceId(campaignId.value(), commerceId.value())
				.map(JpaCampaignMapper::toDomain);
	}

	@Override
	public List<Campaign> findAllByCommerceId(UserId commerceId) {
		return repository.findAllByCommerceId(commerceId.value()).stream()
				.map(JpaCampaignMapper::toDomain)
				.toList();
	}

	@Override
	@Transactional
	public boolean deleteByIdAndCommerceId(CampaignId campaignId, UserId commerceId) {
		return repository.deleteByIdAndCommerceId(campaignId.value(), commerceId.value()) > 0;
	}
}
