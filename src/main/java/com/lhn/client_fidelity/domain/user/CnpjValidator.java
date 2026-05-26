package com.lhn.client_fidelity.domain.user;

import br.com.caelum.stella.validation.InvalidStateException;
import com.lhn.client_fidelity.exception.UserValidationException;

public final class CnpjValidator {

	private final br.com.caelum.stella.validation.CNPJValidator validator =
			new br.com.caelum.stella.validation.CNPJValidator();

	public void validate(String digitsOnlyCnpj) {
		try {
			validator.assertValid(digitsOnlyCnpj);
		}
		catch (InvalidStateException exception) {
			throw UserValidationException.single("governmentIdentifier", "Government identifier must be a valid CNPJ.");
		}
	}
}
