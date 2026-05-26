package com.lhn.client_fidelity.application.authentication;

public interface AccessTokenVerifier {

	AuthenticatedUser verify(String authorizationHeader);
}
