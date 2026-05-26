package com.lhn.client_fidelity.interfaces.rest.user;

import com.lhn.client_fidelity.application.user.CreateUserResult;
import com.lhn.client_fidelity.application.user.CreateUserUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/users")
public class UserController {

	private final CreateUserUseCase createUserUseCase;

	public UserController(CreateUserUseCase createUserUseCase) {
		this.createUserUseCase = createUserUseCase;
	}

	@PostMapping
	public ResponseEntity<UserResponse> create(@RequestBody CreateUserRequest request) {
		CreateUserResult result = createUserUseCase.execute(request.toCommand());
		return ResponseEntity
				.created(URI.create("/users/" + result.id()))
				.body(UserResponse.from(result));
	}
}
