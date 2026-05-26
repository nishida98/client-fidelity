package com.lhn.client_fidelity.infrastructure.authentication.h2;

import com.lhn.client_fidelity.domain.authentication.AuthenticationChallenge;
import com.lhn.client_fidelity.domain.authentication.AuthenticationChallengeId;
import com.lhn.client_fidelity.domain.authentication.AuthenticationCode;
import com.lhn.client_fidelity.domain.user.UserId;

final class JpaAuthenticationChallengeMapper {

	private JpaAuthenticationChallengeMapper() {
	}

	static AuthenticationChallengeEntity toEntity(AuthenticationChallenge challenge) {
		return new AuthenticationChallengeEntity(
				challenge.id().value(),
				challenge.userId().value(),
				challenge.method(),
				challenge.destination(),
				challenge.code().value(),
				challenge.expiresAt(),
				challenge.consumedAt(),
				challenge.attemptCount(),
				challenge.createdAt()
		);
	}

	static AuthenticationChallenge toDomain(AuthenticationChallengeEntity entity) {
		return new AuthenticationChallenge(
				new AuthenticationChallengeId(entity.id()),
				new UserId(entity.userId()),
				entity.method(),
				entity.destination(),
				new AuthenticationCode(entity.code()),
				entity.expiresAt(),
				entity.consumedAt(),
				entity.attemptCount(),
				entity.createdAt()
		);
	}
}
