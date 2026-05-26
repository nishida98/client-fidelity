package com.lhn.client_fidelity.infrastructure.authentication.h2;

import com.lhn.client_fidelity.application.authentication.AuthenticationChallengeRepository;
import com.lhn.client_fidelity.domain.authentication.AuthenticationChallenge;
import com.lhn.client_fidelity.domain.authentication.AuthenticationChallengeId;
import com.lhn.client_fidelity.domain.authentication.AuthenticationMethod;
import com.lhn.client_fidelity.domain.user.UserId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
class JpaAuthenticationChallengeRepository implements AuthenticationChallengeRepository {

	private final JpaAuthenticationChallengeCrudRepository repository;

	JpaAuthenticationChallengeRepository(JpaAuthenticationChallengeCrudRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public AuthenticationChallenge save(AuthenticationChallenge challenge) {
		return JpaAuthenticationChallengeMapper.toDomain(
				repository.saveAndFlush(JpaAuthenticationChallengeMapper.toEntity(challenge))
		);
	}

	@Override
	public Optional<AuthenticationChallenge> findById(AuthenticationChallengeId id) {
		return repository.findById(id.value()).map(JpaAuthenticationChallengeMapper::toDomain);
	}

	@Override
	public Optional<AuthenticationChallenge> findActiveByUserIdAndMethod(
			UserId userId,
			AuthenticationMethod method,
			Instant now
	) {
		return repository
				.findFirstByUserIdAndMethodAndConsumedAtIsNullAndExpiresAtAfter(userId.value(), method, now)
				.map(JpaAuthenticationChallengeMapper::toDomain);
	}
}
