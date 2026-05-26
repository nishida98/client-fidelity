package com.lhn.client_fidelity.exception;

public class CampaignCreationException extends RuntimeException {

	public CampaignCreationException(Throwable cause) {
		super("Campaign could not be created.", cause);
	}
}
