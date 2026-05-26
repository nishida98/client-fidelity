package com.lhn.client_fidelity.application.user;

import com.lhn.client_fidelity.domain.user.Commerce;
import com.lhn.client_fidelity.domain.user.CommerceClient;
import com.lhn.client_fidelity.domain.user.User;
import com.lhn.client_fidelity.domain.user.UserType;

import java.time.Instant;

public record CreateUserResult(
		String id,
		UserType type,
		String name,
		String contactName,
		String email,
		String phone,
		String governmentIdentifier,
		Instant createdAt
) {

	public static CreateUserResult from(User user) {
		if (user instanceof Commerce commerce) {
			return new CreateUserResult(
					commerce.id().value(),
					commerce.type(),
					commerce.name(),
					commerce.contactName(),
					commerce.email().value(),
					commerce.phone().value(),
					commerce.governmentIdentifier().value(),
					commerce.createdAt()
			);
		}
		CommerceClient client = (CommerceClient) user;
		return new CreateUserResult(
				client.id().value(),
				client.type(),
				client.name(),
				null,
				client.email().value(),
				client.phone().value(),
				null,
				client.createdAt()
		);
	}
}
