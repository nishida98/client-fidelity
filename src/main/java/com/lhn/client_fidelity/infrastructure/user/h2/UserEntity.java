package com.lhn.client_fidelity.infrastructure.user.h2;

import com.lhn.client_fidelity.domain.user.UserType;
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
		name = "users",
		indexes = {
				@Index(
						name = "idx_users_commerce_government_identifier",
						columnList = "commerce_government_identifier"
				),
				@Index(
						name = "idx_users_commerce_client_email",
						columnList = "commerce_client_email"
				)
		}
)
class UserEntity {

	@Id
	@Column(name = "id", nullable = false, length = 64)
	private String id;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 32)
	private UserType type;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "contact_name")
	private String contactName;

	@Column(name = "email", nullable = false, length = 320)
	private String email;

	@Column(name = "phone", nullable = false, length = 32)
	private String phone;

	@Column(name = "government_identifier", length = 32)
	private String governmentIdentifier;

	@Column(name = "commerce_government_identifier", unique = true, length = 32)
	private String commerceGovernmentIdentifier;

	@Column(name = "commerce_client_email", unique = true, length = 320)
	private String commerceClientEmail;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected UserEntity() {
	}

	UserEntity(
			String id,
			UserType type,
			String name,
			String contactName,
			String email,
			String phone,
			String governmentIdentifier,
			String commerceGovernmentIdentifier,
			String commerceClientEmail,
			Instant createdAt
	) {
		this.id = id;
		this.type = type;
		this.name = name;
		this.contactName = contactName;
		this.email = email;
		this.phone = phone;
		this.governmentIdentifier = governmentIdentifier;
		this.commerceGovernmentIdentifier = commerceGovernmentIdentifier;
		this.commerceClientEmail = commerceClientEmail;
		this.createdAt = createdAt;
	}

	String id() {
		return id;
	}

	UserType type() {
		return type;
	}

	String name() {
		return name;
	}

	String contactName() {
		return contactName;
	}

	String email() {
		return email;
	}

	String phone() {
		return phone;
	}

	String governmentIdentifier() {
		return governmentIdentifier;
	}

	Instant createdAt() {
		return createdAt;
	}
}
