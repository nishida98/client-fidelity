package com.lhn.client_fidelity.application.user;

import com.lhn.client_fidelity.domain.user.User;

public interface UserRepository {

	boolean existsCommerceByGovernmentIdentifier(String governmentIdentifier);

	boolean existsCommerceClientByEmail(String email);

	User save(User user);
}
