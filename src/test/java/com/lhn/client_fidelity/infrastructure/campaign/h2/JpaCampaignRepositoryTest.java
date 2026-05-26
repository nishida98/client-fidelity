package com.lhn.client_fidelity.infrastructure.campaign.h2;

import tools.jackson.databind.ObjectMapper;
import com.lhn.client_fidelity.domain.campaign.Campaign;
import com.lhn.client_fidelity.domain.campaign.CampaignId;
import com.lhn.client_fidelity.domain.user.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaCampaignRepository.class)
@TestPropertySource(properties = "client-fidelity.persistence.type=h2")
class JpaCampaignRepositoryTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Autowired
	private JpaCampaignRepository repository;

	@Test
	void savesAndFindsCampaignByOwner() throws Exception {
		Campaign campaign = campaign("cam_1", "usr_commerce");
		repository.save(campaign);

		assertThat(repository.findByIdAndCommerceId(new CampaignId("cam_1"), new UserId("usr_commerce")))
				.contains(campaign);
	}

	@Test
	void listsCampaignsByOwner() throws Exception {
		repository.save(campaign("cam_1", "usr_commerce"));
		repository.save(campaign("cam_2", "usr_commerce"));
		repository.save(campaign("cam_3", "usr_other"));

		assertThat(repository.findAllByCommerceId(new UserId("usr_commerce"))).hasSize(2);
	}

	@Test
	void deletesCampaignByOwner() throws Exception {
		repository.save(campaign("cam_1", "usr_commerce"));

		boolean deleted = repository.deleteByIdAndCommerceId(new CampaignId("cam_1"), new UserId("usr_commerce"));

		assertThat(deleted).isTrue();
		assertThat(repository.findByIdAndCommerceId(new CampaignId("cam_1"), new UserId("usr_commerce"))).isEmpty();
	}

	private Campaign campaign(String id, String commerceId) throws Exception {
		return new Campaign(
				new CampaignId(id),
				new UserId(commerceId),
				"Campanha " + id,
				10,
				LocalDate.parse("2026-06-01"),
				LocalDate.parse("2026-06-30"),
				Instant.parse("2026-05-26T13:00:00Z"),
				OBJECT_MAPPER.readTree("{\"title\":\"A\"}")
		);
	}
}

