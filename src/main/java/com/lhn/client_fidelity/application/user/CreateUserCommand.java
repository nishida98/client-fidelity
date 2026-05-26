package com.lhn.client_fidelity.application.user;

public record CreateUserCommand(
		String type,
		String name,
		String contactName,
		boolean contactNamePresent,
		String email,
		String phone,
		String governmentIdentifier,
		boolean governmentIdentifierPresent
) {
}
