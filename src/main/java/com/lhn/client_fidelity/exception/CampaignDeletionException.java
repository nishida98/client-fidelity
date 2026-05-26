package com.lhn.client_fidelity.exception;

public class CampaignDeletionException extends RuntimeException {

	public CampaignDeletionException(Throwable cause) {
		super("Campaign could not be deleted.", cause);
	}
}
