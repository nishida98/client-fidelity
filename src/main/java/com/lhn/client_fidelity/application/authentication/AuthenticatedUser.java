package com.lhn.client_fidelity.application.authentication;

import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.domain.user.UserType;

public record AuthenticatedUser(
		UserId userId,
		UserType userType
) {
}
