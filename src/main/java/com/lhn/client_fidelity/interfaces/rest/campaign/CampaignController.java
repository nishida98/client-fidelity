package com.lhn.client_fidelity.interfaces.rest.campaign;

import com.lhn.client_fidelity.application.authentication.AuthenticatedUser;
import com.lhn.client_fidelity.application.campaign.CreateCampaignResult;
import com.lhn.client_fidelity.application.campaign.CreateCampaignUseCase;
import com.lhn.client_fidelity.application.campaign.DeleteCampaignUseCase;
import com.lhn.client_fidelity.application.campaign.GetCampaignByIdUseCase;
import com.lhn.client_fidelity.application.campaign.ListCommerceCampaignsUseCase;
import com.lhn.client_fidelity.application.campaign.PatchCampaignUseCase;
import com.lhn.client_fidelity.application.campaign.ReplaceCampaignUseCase;
import com.lhn.client_fidelity.domain.user.UserType;
import com.lhn.client_fidelity.exception.ForbiddenUserTypeException;
import com.lhn.client_fidelity.interfaces.rest.authentication.JwtAuthenticationInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/campaigns")
public class CampaignController {

	private final CreateCampaignUseCase createCampaignUseCase;
	private final GetCampaignByIdUseCase getCampaignByIdUseCase;
	private final ListCommerceCampaignsUseCase listCommerceCampaignsUseCase;
	private final ReplaceCampaignUseCase replaceCampaignUseCase;
	private final PatchCampaignUseCase patchCampaignUseCase;
	private final DeleteCampaignUseCase deleteCampaignUseCase;

	public CampaignController(
			CreateCampaignUseCase createCampaignUseCase,
			GetCampaignByIdUseCase getCampaignByIdUseCase,
			ListCommerceCampaignsUseCase listCommerceCampaignsUseCase,
			ReplaceCampaignUseCase replaceCampaignUseCase,
			PatchCampaignUseCase patchCampaignUseCase,
			DeleteCampaignUseCase deleteCampaignUseCase
	) {
		this.createCampaignUseCase = createCampaignUseCase;
		this.getCampaignByIdUseCase = getCampaignByIdUseCase;
		this.listCommerceCampaignsUseCase = listCommerceCampaignsUseCase;
		this.replaceCampaignUseCase = replaceCampaignUseCase;
		this.patchCampaignUseCase = patchCampaignUseCase;
		this.deleteCampaignUseCase = deleteCampaignUseCase;
	}

	@PostMapping
	public ResponseEntity<CampaignResponse> create(
			@RequestBody CreateCampaignRequest request,
			HttpServletRequest httpRequest
	) {
		AuthenticatedUser authenticatedUser = requireCommerceUser(httpRequest);
		CreateCampaignResult result = createCampaignUseCase.execute(
				request.toCommand(authenticatedUser.userId().value())
		);
		return ResponseEntity.created(URI.create("/campaigns/" + result.id()))
				.body(CampaignResponse.from(result));
	}

	@GetMapping("/me")
	public ResponseEntity<List<CampaignResponse>> listMine(HttpServletRequest httpRequest) {
		AuthenticatedUser authenticatedUser = requireCommerceUser(httpRequest);
		List<CampaignResponse> response = listCommerceCampaignsUseCase.execute(authenticatedUser.userId().value())
				.stream()
				.map(CampaignResponse::from)
				.toList();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<CampaignResponse> getById(@PathVariable String id, HttpServletRequest httpRequest) {
		AuthenticatedUser authenticatedUser = requireCommerceUser(httpRequest);
		return ResponseEntity.ok(CampaignResponse.from(
				getCampaignByIdUseCase.execute(id, authenticatedUser.userId().value())
		));
	}

	@PutMapping("/{id}")
	public ResponseEntity<CampaignResponse> replace(
			@PathVariable String id,
			@RequestBody ReplaceCampaignRequest request,
			HttpServletRequest httpRequest
	) {
		AuthenticatedUser authenticatedUser = requireCommerceUser(httpRequest);
		return ResponseEntity.ok(CampaignResponse.from(
				replaceCampaignUseCase.execute(request.toCommand(id, authenticatedUser.userId().value()))
		));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<CampaignResponse> patch(
			@PathVariable String id,
			@RequestBody PatchCampaignRequest request,
			HttpServletRequest httpRequest
	) {
		AuthenticatedUser authenticatedUser = requireCommerceUser(httpRequest);
		return ResponseEntity.ok(CampaignResponse.from(
				patchCampaignUseCase.execute(request.toCommand(id, authenticatedUser.userId().value()))
		));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable String id, HttpServletRequest httpRequest) {
		AuthenticatedUser authenticatedUser = requireCommerceUser(httpRequest);
		deleteCampaignUseCase.execute(id, authenticatedUser.userId().value());
		return ResponseEntity.noContent().build();
	}

	private AuthenticatedUser requireCommerceUser(HttpServletRequest httpRequest) {
		Object attribute = httpRequest.getAttribute(JwtAuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE);
		if (!(attribute instanceof AuthenticatedUser authenticatedUser)) {
			throw new IllegalStateException("Authenticated user is required.");
		}
		if (authenticatedUser.userType() != UserType.COMMERCE) {
			throw new ForbiddenUserTypeException();
		}
		return authenticatedUser;
	}
}
