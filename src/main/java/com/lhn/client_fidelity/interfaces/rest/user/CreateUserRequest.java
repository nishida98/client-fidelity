package com.lhn.client_fidelity.interfaces.rest.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.lhn.client_fidelity.application.user.CreateUserCommand;
import com.lhn.client_fidelity.domain.user.FieldValidationError;
import com.lhn.client_fidelity.exception.UserValidationException;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public class CreateUserRequest {

	private String type;
	private String name;
	private String contactName;
	private boolean contactNamePresent;
	private String email;
	private String phone;
	private String governmentIdentifier;
	private boolean governmentIdentifierPresent;
	private final List<String> unknownFields = new ArrayList<>();

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
		this.contactNamePresent = true;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getGovernmentIdentifier() {
		return governmentIdentifier;
	}

	public void setGovernmentIdentifier(String governmentIdentifier) {
		this.governmentIdentifier = governmentIdentifier;
		this.governmentIdentifierPresent = true;
	}

	@JsonAnySetter
	public void setUnknownField(String field, Object value) {
		unknownFields.add(field);
	}

	CreateUserCommand toCommand() {
		if (!unknownFields.isEmpty()) {
			throw new UserValidationException(unknownFields.stream()
					.map(field -> new FieldValidationError(field, "Field is not allowed."))
					.toList());
		}
		return new CreateUserCommand(
				type,
				name,
				contactName,
				contactNamePresent,
				email,
				phone,
				governmentIdentifier,
				governmentIdentifierPresent
		);
	}
}
