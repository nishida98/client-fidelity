package com.lhn.client_fidelity.infrastructure.user.h2;

import org.springframework.data.jpa.repository.JpaRepository;

interface JpaUserCrudRepository extends JpaRepository<UserEntity, String> {

	boolean existsByTypeAndGovernmentIdentifier(com.lhn.client_fidelity.domain.user.UserType type, String governmentIdentifier);

	boolean existsByTypeAndEmail(com.lhn.client_fidelity.domain.user.UserType type, String email);
}
