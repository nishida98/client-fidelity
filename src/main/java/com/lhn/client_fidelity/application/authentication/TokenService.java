package com.lhn.client_fidelity.application.authentication;

import com.lhn.client_fidelity.domain.user.User;

import java.time.Instant;

public interface TokenService {

	TokenResult createToken(User user, Instant issuedAt);
}
