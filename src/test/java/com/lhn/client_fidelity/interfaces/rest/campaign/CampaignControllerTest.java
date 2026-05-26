package com.lhn.client_fidelity.interfaces.rest.campaign;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.lhn.client_fidelity.application.authentication.AccessTokenVerifier;
import com.lhn.client_fidelity.application.authentication.AuthenticatedUser;
import com.lhn.client_fidelity.application.campaign.CampaignRepository;
import com.lhn.client_fidelity.application.campaign.CreateCampaignUseCase;
import com.lhn.client_fidelity.application.campaign.DeleteCampaignUseCase;
import com.lhn.client_fidelity.application.campaign.GetCampaignByIdUseCase;
import com.lhn.client_fidelity.application.campaign.ListCommerceCampaignsUseCase;
import com.lhn.client_fidelity.application.campaign.PatchCampaignUseCase;
import com.lhn.client_fidelity.application.campaign.ReplaceCampaignUseCase;
import com.lhn.client_fidelity.domain.campaign.Campaign;
import com.lhn.client_fidelity.domain.campaign.CampaignId;
import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.domain.user.UserType;
import com.lhn.client_fidelity.exception.InvalidAccessTokenException;
import com.lhn.client_fidelity.exception.MissingAccessTokenException;
import com.lhn.client_fidelity.interfaces.rest.authentication.JwtAuthenticationInterceptor;
import com.lhn.client_fidelity.interfaces.rest.error.RestExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CampaignControllerTest {

	private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-26T13:00:00Z"), ZoneOffset.UTC);

	private InMemoryCampaignRepository repository;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		repository = new InMemoryCampaignRepository();
		CampaignController controller = new CampaignController(
				new CreateCampaignUseCase(repository, CLOCK),
				new GetCampaignByIdUseCase(repository),
				new ListCommerceCampaignsUseCase(repository),
				new ReplaceCampaignUseCase(repository, CLOCK),
				new PatchCampaignUseCase(repository, CLOCK),
				new DeleteCampaignUseCase(repository)
		);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new RestExceptionHandler(CLOCK))
				.addInterceptors(new JwtAuthenticationInterceptor(new FakeAccessTokenVerifier()))
				.build();
	}

	@Test
	void createsCampaign() throws Exception {
		mockMvc.perform(post("/campaigns")
						.header("Authorization", "Bearer commerce-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Campanha de Inverno",
								  "points": 0,
								  "startDate": "2026-06-01",
								  "expirationDate": "2026-06-30",
								  "template": {
								    "title": "Ganhe pontos"
								  }
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", notNullValue()))
				.andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.points").value(0));
	}

	@Test
	void getsCampaignById() throws Exception {
		repository.save(existingCampaign("cam_1", "usr_commerce"));

		mockMvc.perform(get("/campaigns/cam_1")
						.header("Authorization", "Bearer commerce-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("cam_1"));
	}

	@Test
	void listsCampaigns() throws Exception {
		repository.save(existingCampaign("cam_1", "usr_commerce"));

		mockMvc.perform(get("/campaigns/me")
						.header("Authorization", "Bearer commerce-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("cam_1"));
	}

	@Test
	void returnsEmptyListWhenNoCampaignsExist() throws Exception {
		mockMvc.perform(get("/campaigns/me")
						.header("Authorization", "Bearer commerce-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}

	@Test
	void replacesCampaign() throws Exception {
		repository.save(existingCampaign("cam_1", "usr_commerce"));

		mockMvc.perform(put("/campaigns/cam_1")
						.header("Authorization", "Bearer commerce-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Atualizada",
								  "points": 20,
								  "startDate": "2026-07-01",
								  "expirationDate": "2026-07-31",
								  "template": {
								    "title": "Nova"
								  }
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Atualizada"));
	}

	@Test
	void patchesCampaign() throws Exception {
		repository.save(existingCampaign("cam_1", "usr_commerce"));

		mockMvc.perform(patch("/campaigns/cam_1")
						.header("Authorization", "Bearer commerce-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "points": 25
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.points").value(25));
	}

	@Test
	void deletesCampaign() throws Exception {
		repository.save(existingCampaign("cam_1", "usr_commerce"));

		mockMvc.perform(delete("/campaigns/cam_1")
						.header("Authorization", "Bearer commerce-token"))
				.andExpect(status().isNoContent());
	}

	@Test
	void rejectsMissingToken() throws Exception {
		mockMvc.perform(get("/campaigns/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("MISSING_ACCESS_TOKEN"));
	}

	@Test
	void rejectsInvalidToken() throws Exception {
		mockMvc.perform(get("/campaigns/me")
						.header("Authorization", "Bearer invalid-token"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_ACCESS_TOKEN"));
	}

	@Test
	void rejectsCommerceClientToken() throws Exception {
		mockMvc.perform(get("/campaigns/me")
						.header("Authorization", "Bearer client-token"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN_USER_TYPE"));
	}

	@Test
	void returnsNotFoundForForeignCampaign() throws Exception {
		repository.save(existingCampaign("cam_1", "usr_other"));

		mockMvc.perform(get("/campaigns/cam_1")
						.header("Authorization", "Bearer commerce-token"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("CAMPAIGN_NOT_FOUND"));
	}

	@Test
	void returnsValidationFailureForUnknownField() throws Exception {
		mockMvc.perform(post("/campaigns")
						.header("Authorization", "Bearer commerce-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Campanha",
								  "points": 0,
								  "startDate": "2026-06-01",
								  "expirationDate": "2026-06-30",
								  "template": {},
								  "unexpected": "value"
								}
								"""))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
	}

	private Campaign existingCampaign(String id, String commerceId) {
		ObjectNode template = new ObjectMapper().createObjectNode();
		template.put("title", "Original");
		return new Campaign(
				new CampaignId(id),
				new UserId(commerceId),
				"Campanha",
				10,
				LocalDate.parse("2026-06-01"),
				LocalDate.parse("2026-06-30"),
				Instant.parse("2026-05-26T12:00:00Z"),
				template
		);
	}

	private class FakeAccessTokenVerifier implements AccessTokenVerifier {

		@Override
		public AuthenticatedUser verify(String authorizationHeader) {
			if (authorizationHeader == null || authorizationHeader.isBlank()) {
				throw new MissingAccessTokenException();
			}
			if ("Bearer commerce-token".equals(authorizationHeader)) {
				return new AuthenticatedUser(new UserId("usr_commerce"), UserType.COMMERCE);
			}
			if ("Bearer client-token".equals(authorizationHeader)) {
				return new AuthenticatedUser(new UserId("usr_client"), UserType.COMMERCE_CLIENT);
			}
			throw new InvalidAccessTokenException();
		}
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
			Campaign campaign = campaigns.get(campaignId.value());
			if (campaign == null || !campaign.commerceId().equals(commerceId)) {
				return false;
			}
			campaigns.remove(campaignId.value());
			return true;
		}
	}
}

