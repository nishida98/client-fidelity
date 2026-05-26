package com.lhn.client_fidelity.infrastructure.authentication.h2;

import com.lhn.client_fidelity.domain.authentication.AuthenticationMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

interface JpaAuthenticationChallengeCrudRepository extends JpaRepository<AuthenticationChallengeEntity, String> {

	Optional<AuthenticationChallengeEntity> findFirstByUserIdAndMethodAndConsumedAtIsNullAndExpiresAtAfter(
			String userId,
			AuthenticationMethod method,
			Instant now
	);
}
