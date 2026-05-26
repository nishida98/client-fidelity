package com.lhn.client_fidelity.application.user;

import com.lhn.client_fidelity.domain.user.User;

import java.util.Optional;

public interface UserRepository {

	boolean existsCommerceByGovernmentIdentifier(String governmentIdentifier);

	boolean existsCommerceClientByEmail(String email);

	Optional<User> findByEmail(String email);

	Optional<User> findByPhone(String phone);

	User save(User user);
}
