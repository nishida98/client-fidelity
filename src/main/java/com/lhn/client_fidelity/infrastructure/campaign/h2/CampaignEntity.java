package com.lhn.client_fidelity.infrastructure.campaign.h2;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
		name = "campaigns",
		indexes = {
				@Index(name = "idx_campaigns_commerce_id", columnList = "commerce_id")
		}
)
class CampaignEntity {

	@Id
	@Column(name = "id", nullable = false, length = 64)
	private String id;

	@Column(name = "commerce_id", nullable = false, length = 64)
	private String commerceId;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "points", nullable = false)
	private int points;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "expiration_date", nullable = false)
	private LocalDate expirationDate;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "template_payload", nullable = false, length = 4000)
	private String templatePayload;

	protected CampaignEntity() {
	}

	CampaignEntity(
			String id,
			String commerceId,
			String name,
			int points,
			LocalDate startDate,
			LocalDate expirationDate,
			Instant updatedAt,
			String templatePayload
	) {
		this.id = id;
		this.commerceId = commerceId;
		this.name = name;
		this.points = points;
		this.startDate = startDate;
		this.expirationDate = expirationDate;
		this.updatedAt = updatedAt;
		this.templatePayload = templatePayload;
	}

	String id() {
		return id;
	}

	String commerceId() {
		return commerceId;
	}

	String name() {
		return name;
	}

	int points() {
		return points;
	}

	LocalDate startDate() {
		return startDate;
	}

	LocalDate expirationDate() {
		return expirationDate;
	}

	Instant updatedAt() {
		return updatedAt;
	}

	String templatePayload() {
		return templatePayload;
	}
}
