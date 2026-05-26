package com.lhn.client_fidelity.infrastructure.authentication.delivery;

import com.lhn.client_fidelity.domain.authentication.AuthenticationMethod;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleEmailAuthenticationCodeDeliveryTest {

	@Test
	void writesCodeToConsole() {
		ConsoleEmailAuthenticationCodeDelivery delivery = new ConsoleEmailAuthenticationCodeDelivery();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream originalOut = System.out;
		System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
		try {
			delivery.send("user@email.com", "123456");
		}
		finally {
			System.setOut(originalOut);
		}

		assertThat(output.toString(StandardCharsets.UTF_8)).contains("user@email.com", "123456");
		assertThat(delivery.supports(AuthenticationMethod.EMAIL, "console")).isTrue();
	}
}
