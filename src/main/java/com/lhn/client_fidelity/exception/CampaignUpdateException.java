package com.lhn.client_fidelity.exception;

public class CampaignUpdateException extends RuntimeException {

	public CampaignUpdateException(Throwable cause) {
		super("Campaign could not be updated.", cause);
	}
}
