package com.lhn.client_fidelity.application.authentication;

import com.lhn.client_fidelity.domain.authentication.AuthenticationChallenge;
import com.lhn.client_fidelity.domain.authentication.AuthenticationChallengeId;
import com.lhn.client_fidelity.domain.authentication.AuthenticationMethod;
import com.lhn.client_fidelity.domain.user.UserId;

import java.time.Instant;
import java.util.Optional;

public interface AuthenticationChallengeRepository {

	AuthenticationChallenge save(AuthenticationChallenge challenge);

	Optional<AuthenticationChallenge> findById(AuthenticationChallengeId id);

	Optional<AuthenticationChallenge> findActiveByUserIdAndMethod(
			UserId userId,
			AuthenticationMethod method,
			Instant now
	);
}
