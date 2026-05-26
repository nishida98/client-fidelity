package com.lhn.client_fidelity.exception;

public class PhoneDeliveryNotConfiguredException extends RuntimeException {

	public PhoneDeliveryNotConfiguredException() {
		super("Phone authentication delivery is not configured.");
	}
}
