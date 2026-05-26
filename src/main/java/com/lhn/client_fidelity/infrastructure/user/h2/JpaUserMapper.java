package com.lhn.client_fidelity.infrastructure.user.h2;

import com.lhn.client_fidelity.domain.user.Commerce;
import com.lhn.client_fidelity.domain.user.CommerceClient;
import com.lhn.client_fidelity.domain.user.Email;
import com.lhn.client_fidelity.domain.user.GovernmentIdentifier;
import com.lhn.client_fidelity.domain.user.Phone;
import com.lhn.client_fidelity.domain.user.User;
import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.domain.user.UserType;

final class JpaUserMapper {

	private JpaUserMapper() {
	}

	static UserEntity toEntity(User user) {
		if (user instanceof Commerce commerce) {
			return new UserEntity(
					commerce.id().value(),
					commerce.type(),
					commerce.name(),
					commerce.contactName(),
					commerce.email().value(),
					commerce.phone().value(),
					commerce.governmentIdentifier().value(),
					commerce.governmentIdentifier().value(),
					null,
					commerce.createdAt()
			);
		}
		CommerceClient client = (CommerceClient) user;
		return new UserEntity(
				client.id().value(),
				client.type(),
				client.name(),
				null,
				client.email().value(),
				client.phone().value(),
				null,
				null,
				client.email().value(),
				client.createdAt()
		);
	}

	static User toDomain(UserEntity entity) {
		if (entity.type() == UserType.COMMERCE) {
			return new Commerce(
					new UserId(entity.id()),
					entity.name(),
					entity.contactName(),
					new Email(entity.email()),
					new Phone(entity.phone()),
					new GovernmentIdentifier(entity.governmentIdentifier()),
					entity.createdAt()
			);
		}
		return new CommerceClient(
				new UserId(entity.id()),
				entity.name(),
				new Email(entity.email()),
				new Phone(entity.phone()),
				entity.createdAt()
		);
	}
}
