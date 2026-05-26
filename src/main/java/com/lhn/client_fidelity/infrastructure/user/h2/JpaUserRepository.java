package com.lhn.client_fidelity.infrastructure.user.h2;

import com.lhn.client_fidelity.application.user.UserRepository;
import com.lhn.client_fidelity.domain.user.User;
import com.lhn.client_fidelity.domain.user.UserType;
import com.lhn.client_fidelity.exception.DuplicateUserPersistenceException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "client-fidelity.persistence.type", havingValue = "h2", matchIfMissing = true)
class JpaUserRepository implements UserRepository {

	private final JpaUserCrudRepository repository;

	JpaUserRepository(JpaUserCrudRepository repository) {
		this.repository = repository;
	}

	@Override
	public boolean existsCommerceByGovernmentIdentifier(String governmentIdentifier) {
		return repository.existsByTypeAndGovernmentIdentifier(UserType.COMMERCE, governmentIdentifier);
	}

	@Override
	public boolean existsCommerceClientByEmail(String email) {
		return repository.existsByTypeAndEmail(UserType.COMMERCE_CLIENT, email);
	}

	@Override
	@Transactional
	public User save(User user) {
		try {
			UserEntity savedEntity = repository.saveAndFlush(JpaUserMapper.toEntity(user));
			return JpaUserMapper.toDomain(savedEntity);
		}
		catch (DataIntegrityViolationException exception) {
			throw new DuplicateUserPersistenceException(exception);
		}
	}
}
