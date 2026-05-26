package com.lhn.client_fidelity.application.campaign;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.lhn.client_fidelity.domain.campaign.Campaign;
import com.lhn.client_fidelity.domain.campaign.CampaignId;
import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.exception.CampaignNotFoundException;
import com.lhn.client_fidelity.exception.CampaignValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CampaignMutationUseCaseTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-26T13:00:00Z"), ZoneOffset.UTC);

	private InMemoryCampaignRepository repository;

	@BeforeEach
	void setUp() {
		repository = new InMemoryCampaignRepository();
		repository.save(existingCampaign());
	}

	@Test
	void getsCampaignByOwner() {
		CreateCampaignResult result = new GetCampaignByIdUseCase(repository).execute("cam_1", "usr_commerce");

		assertThat(result.id()).isEqualTo("cam_1");
	}

	@Test
	void rejectsForeignCampaignLookup() {
		assertThatThrownBy(() -> new GetCampaignByIdUseCase(repository).execute("cam_1", "usr_other"))
				.isInstanceOf(CampaignNotFoundException.class);
	}

	@Test
	void listsCampaignsByOwner() {
		List<CreateCampaignResult> results = new ListCommerceCampaignsUseCase(repository).execute("usr_commerce");

		assertThat(results).hasSize(1);
	}

	@Test
	void replacesCampaign() throws Exception {
		ReplaceCampaignUseCase useCase = new ReplaceCampaignUseCase(repository, CLOCK);

		CreateCampaignResult result = useCase.execute(new ReplaceCampaignCommand(
				"cam_1",
				"usr_commerce",
				"Novo nome",
				15,
				"2026-07-01",
				"2026-07-30",
				OBJECT_MAPPER.readTree("{\"color\":\"blue\"}")
		));

		assertThat(result.name()).isEqualTo("Novo nome");
		assertThat(result.points()).isEqualTo(15);
	}

	@Test
	void patchesCampaign() {
		PatchCampaignUseCase useCase = new PatchCampaignUseCase(repository, CLOCK);

		CreateCampaignResult result = useCase.execute(new PatchCampaignCommand(
				"cam_1",
				"usr_commerce",
				"Atualizada",
				true,
				null,
				false,
				null,
				false,
				null,
				false,
				null,
				false
		));

		assertThat(result.name()).isEqualTo("Atualizada");
		assertThat(result.points()).isEqualTo(10);
	}

	@Test
	void rejectsEmptyPatch() {
		PatchCampaignUseCase useCase = new PatchCampaignUseCase(repository, CLOCK);

		assertThatThrownBy(() -> useCase.execute(new PatchCampaignCommand(
				"cam_1",
				"usr_commerce",
				null,
				false,
				null,
				false,
				null,
				false,
				null,
				false,
				null,
				false
		))).isInstanceOf(CampaignValidationException.class);
	}

	@Test
	void deletesCampaign() {
		DeleteCampaignUseCase useCase = new DeleteCampaignUseCase(repository);

		useCase.execute("cam_1", "usr_commerce");

		assertThat(repository.findByIdAndCommerceId(new CampaignId("cam_1"), new UserId("usr_commerce"))).isEmpty();
	}

	private Campaign existingCampaign() {
		ObjectNode template = OBJECT_MAPPER.createObjectNode();
		template.put("title", "Original");
		return new Campaign(
				new CampaignId("cam_1"),
				new UserId("usr_commerce"),
				"Campanha",
				10,
				LocalDate.parse("2026-06-01"),
				LocalDate.parse("2026-06-30"),
				Instant.parse("2026-05-26T12:00:00Z"),
				template
		);
	}

	private static class InMemoryCampaignRepository implements CampaignRepository {

		private final Map<String, Campaign> campaigns = new LinkedHashMap<>();

		@Override
		public Campaign save(Campaign campaign) {
			campaigns.put(campaign.id().value(), campaign);
			return campaign;
		}

		@Override
		public Optional<Campaign> findByIdAndCommerceId(CampaignId campaignId, UserId commerceId) {
			Campaign campaign = campaigns.get(campaignId.value());
			if (campaign == null || !campaign.commerceId().equals(commerceId)) {
				return Optional.empty();
			}
			return Optional.of(campaign);
		}

		@Override
		public List<Campaign> findAllByCommerceId(UserId commerceId) {
			return campaigns.values().stream()
					.filter(campaign -> campaign.commerceId().equals(commerceId))
					.toList();
		}

		@Override
		public boolean deleteByIdAndCommerceId(CampaignId campaignId, UserId commerceId) {
			return campaigns.remove(campaignId.value(), campaigns.get(campaignId.value()))
					&& !campaigns.containsKey(campaignId.value());
		}
	}
}

