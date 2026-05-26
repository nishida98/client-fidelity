package com.lhn.client_fidelity.interfaces.rest.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lhn.client_fidelity.application.user.CreateUserResult;
import com.lhn.client_fidelity.domain.user.UserType;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
		String id,
		UserType type,
		String name,
		String contactName,
		String email,
		String phone,
		String governmentIdentifier,
		Instant createdAt
) {

	static UserResponse from(CreateUserResult result) {
		return new UserResponse(
				result.id(),
				result.type(),
				result.name(),
				result.contactName(),
				result.email(),
				result.phone(),
				result.governmentIdentifier(),
				result.createdAt()
		);
	}
}
