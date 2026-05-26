package com.lhn.client_fidelity.application.campaign;

import tools.jackson.databind.ObjectMapper;
import com.lhn.client_fidelity.domain.campaign.Campaign;
import com.lhn.client_fidelity.domain.campaign.CampaignId;
import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.exception.CampaignValidationException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateCampaignUseCaseTest {

	private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-26T13:00:00Z"), ZoneOffset.UTC);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final InMemoryCampaignRepository repository = new InMemoryCampaignRepository();
	private final CreateCampaignUseCase useCase = new CreateCampaignUseCase(repository, CLOCK);

	@Test
	void createsCampaign() throws Exception {
		CreateCampaignResult result = useCase.execute(new CreateCampaignCommand(
				"usr_commerce",
				"Campanha",
				0,
				"2026-06-01",
				"2026-06-30",
				OBJECT_MAPPER.readTree("{\"title\":\"A\"}")
		));

		assertThat(result.id()).startsWith("cam_");
		assertThat(result.points()).isZero();
		assertThat(result.updatedAt()).isEqualTo(CLOCK.instant());
		assertThat(repository.savedCampaigns).hasSize(1);
	}

	@Test
	void rejectsNegativePoints() throws Exception {
		assertThatThrownBy(() -> useCase.execute(new CreateCampaignCommand(
				"usr_commerce",
				"Campanha",
				-1,
				"2026-06-01",
				"2026-06-30",
				OBJECT_MAPPER.readTree("{\"title\":\"A\"}")
		))).isInstanceOf(CampaignValidationException.class);
	}

	@Test
	void rejectsDateOrder() throws Exception {
		assertThatThrownBy(() -> useCase.execute(new CreateCampaignCommand(
				"usr_commerce",
				"Campanha",
				10,
				"2026-06-30",
				"2026-06-01",
				OBJECT_MAPPER.readTree("{\"title\":\"A\"}")
		))).isInstanceOf(CampaignValidationException.class);
	}

	@Test
	void rejectsNonObjectTemplate() throws Exception {
		assertThatThrownBy(() -> useCase.execute(new CreateCampaignCommand(
				"usr_commerce",
				"Campanha",
				10,
				"2026-06-01",
				"2026-06-30",
				OBJECT_MAPPER.readTree("\"template\"")
		))).isInstanceOf(CampaignValidationException.class);
	}

	private static class InMemoryCampaignRepository implements CampaignRepository {

		private final List<Campaign> savedCampaigns = new ArrayList<>();

		@Override
		public Campaign save(Campaign campaign) {
			savedCampaigns.add(campaign);
			return campaign;
		}

		@Override
		public Optional<Campaign> findByIdAndCommerceId(CampaignId campaignId, UserId commerceId) {
			return Optional.empty();
		}

		@Override
		public List<Campaign> findAllByCommerceId(UserId commerceId) {
			return List.of();
		}

		@Override
		public boolean deleteByIdAndCommerceId(CampaignId campaignId, UserId commerceId) {
			return false;
		}
	}
}

