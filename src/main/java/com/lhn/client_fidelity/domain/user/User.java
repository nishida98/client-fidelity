package com.lhn.client_fidelity.domain.user;

import java.time.Instant;

public sealed interface User permits Commerce, CommerceClient {

	UserId id();

	UserType type();

	String name();

	Email email();

	Phone phone();

	Instant createdAt();
}
