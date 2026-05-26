package com.lhn.client_fidelity.exception;

public class CampaignNotFoundException extends RuntimeException {

	public CampaignNotFoundException() {
		super("Campaign was not found.");
	}
}
