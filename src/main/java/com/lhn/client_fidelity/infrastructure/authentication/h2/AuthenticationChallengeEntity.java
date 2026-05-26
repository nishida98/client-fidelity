package com.lhn.client_fidelity.infrastructure.authentication.h2;

import com.lhn.client_fidelity.domain.authentication.AuthenticationMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
		name = "authentication_challenges",
		indexes = {
				@Index(name = "idx_auth_challenges_user_method", columnList = "user_id, method"),
				@Index(name = "idx_auth_challenges_expires_at", columnList = "expires_at"),
				@Index(name = "idx_auth_challenges_consumed_at", columnList = "consumed_at")
		}
)
class AuthenticationChallengeEntity {

	@Id
	@Column(name = "id", nullable = false, length = 64)
	private String id;

	@Column(name = "user_id", nullable = false, length = 64)
	private String userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "method", nullable = false, length = 32)
	private AuthenticationMethod method;

	@Column(name = "destination", nullable = false, length = 320)
	private String destination;

	@Column(name = "code", nullable = false, length = 6)
	private String code;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "consumed_at")
	private Instant consumedAt;

	@Column(name = "attempt_count", nullable = false)
	private int attemptCount;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected AuthenticationChallengeEntity() {
	}

	AuthenticationChallengeEntity(
			String id,
			String userId,
			AuthenticationMethod method,
			String destination,
			String code,
			Instant expiresAt,
			Instant consumedAt,
			int attemptCount,
			Instant createdAt
	) {
		this.id = id;
		this.userId = userId;
		this.method = method;
		this.destination = destination;
		this.code = code;
		this.expiresAt = expiresAt;
		this.consumedAt = consumedAt;
		this.attemptCount = attemptCount;
		this.createdAt = createdAt;
	}

	String id() { return id; }
	String userId() { return userId; }
	AuthenticationMethod method() { return method; }
	String destination() { return destination; }
	String code() { return code; }
	Instant expiresAt() { return expiresAt; }
	Instant consumedAt() { return consumedAt; }
	int attemptCount() { return attemptCount; }
	Instant createdAt() { return createdAt; }
}
