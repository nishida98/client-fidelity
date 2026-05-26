package com.lhn.client_fidelity.interfaces.rest.authentication;

import com.lhn.client_fidelity.application.authentication.RequestAuthenticationCodeResult;
import com.lhn.client_fidelity.application.authentication.RequestAuthenticationCodeUseCase;
import com.lhn.client_fidelity.application.authentication.TokenResult;
import com.lhn.client_fidelity.application.authentication.VerifyAuthenticationCodeUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

	private final RequestAuthenticationCodeUseCase requestCodeUseCase;
	private final VerifyAuthenticationCodeUseCase verifyCodeUseCase;

	public AuthenticationController(
			RequestAuthenticationCodeUseCase requestCodeUseCase,
			VerifyAuthenticationCodeUseCase verifyCodeUseCase
	) {
		this.requestCodeUseCase = requestCodeUseCase;
		this.verifyCodeUseCase = verifyCodeUseCase;
	}

	@PostMapping("/codes")
	public ResponseEntity<RequestAuthenticationCodeResponse> requestCode(
			@RequestBody RequestAuthenticationCodeRequest request
	) {
		RequestAuthenticationCodeResult result = requestCodeUseCase.execute(request.toCommand());
		return ResponseEntity.accepted().body(RequestAuthenticationCodeResponse.from(result));
	}

	@PostMapping("/token")
	public ResponseEntity<TokenResponse> token(@RequestBody VerifyAuthenticationCodeRequest request) {
		TokenResult result = verifyCodeUseCase.execute(request.toCommand());
		return ResponseEntity.ok(TokenResponse.from(result));
	}
}
