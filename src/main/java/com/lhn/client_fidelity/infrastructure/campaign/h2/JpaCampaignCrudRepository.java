package com.lhn.client_fidelity.infrastructure.campaign.h2;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface JpaCampaignCrudRepository extends JpaRepository<CampaignEntity, String> {

	Optional<CampaignEntity> findByIdAndCommerceId(String id, String commerceId);

	List<CampaignEntity> findAllByCommerceId(String commerceId);

	long deleteByIdAndCommerceId(String id, String commerceId);
}
