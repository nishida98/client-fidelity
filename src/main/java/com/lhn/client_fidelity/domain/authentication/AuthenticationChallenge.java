package com.lhn.client_fidelity.domain.authentication;

import com.lhn.client_fidelity.domain.user.UserId;

import java.time.Instant;

public class AuthenticationChallenge {

	private final AuthenticationChallengeId id;
	private final UserId userId;
	private final AuthenticationMethod method;
	private final String destination;
	private final AuthenticationCode code;
	private final Instant expiresAt;
	private final Instant createdAt;
	private Instant consumedAt;
	private int attemptCount;

	public AuthenticationChallenge(
			AuthenticationChallengeId id,
			UserId userId,
			AuthenticationMethod method,
			String destination,
			AuthenticationCode code,
			Instant expiresAt,
			Instant consumedAt,
			int attemptCount,
			Instant createdAt
	) {
		if (id == null || userId == null || method == null || code == null || expiresAt == null || createdAt == null) {
			throw new IllegalArgumentException("Authentication challenge required fields must not be null");
		}
		if (destination == null || destination.isBlank()) {
			throw new IllegalArgumentException("Authentication challenge destination must not be blank");
		}
		if (attemptCount < 0) {
			throw new IllegalArgumentException("Authentication challenge attempt count must not be negative");
		}
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

	public static AuthenticationChallenge create(
			UserId userId,
			AuthenticationMethod method,
			String destination,
			AuthenticationCode code,
			Instant now,
			java.time.Duration expiration
	) {
		return new AuthenticationChallenge(
				AuthenticationChallengeId.newId(),
				userId,
				method,
				destination,
				code,
				now.plus(expiration),
				null,
				0,
				now
		);
	}

	public boolean isExpired(Instant now) {
		return !expiresAt.isAfter(now);
	}

	public boolean isConsumed() {
		return consumedAt != null;
	}

	public boolean isLocked(int maxFailedAttempts) {
		return attemptCount >= maxFailedAttempts;
	}

	public boolean matches(AuthenticationCode submittedCode) {
		return code.value().equals(submittedCode.value());
	}

	public void consume(Instant now) {
		consumedAt = now;
	}

	public void recordFailedAttempt() {
		attemptCount++;
	}

	public AuthenticationChallengeId id() {
		return id;
	}

	public UserId userId() {
		return userId;
	}

	public AuthenticationMethod method() {
		return method;
	}

	public String destination() {
		return destination;
	}

	public AuthenticationCode code() {
		return code;
	}

	public Instant expiresAt() {
		return expiresAt;
	}

	public Instant consumedAt() {
		return consumedAt;
	}

	public int attemptCount() {
		return attemptCount;
	}

	public Instant createdAt() {
		return createdAt;
	}
}
