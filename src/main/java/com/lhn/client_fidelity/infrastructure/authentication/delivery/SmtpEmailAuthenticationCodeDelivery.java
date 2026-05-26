package com.lhn.client_fidelity.infrastructure.authentication.delivery;

import com.lhn.client_fidelity.application.authentication.AuthenticationCodeDelivery;
import com.lhn.client_fidelity.domain.authentication.AuthenticationMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "client-fidelity.authentication.email-provider", havingValue = "smtp")
class SmtpEmailAuthenticationCodeDelivery implements AuthenticationCodeDelivery {

	private final JavaMailSender mailSender;
	private final String sender;

	SmtpEmailAuthenticationCodeDelivery(
			JavaMailSender mailSender,
			@Value("${client-fidelity.authentication.smtp.sender}") String sender
	) {
		this.mailSender = mailSender;
		this.sender = sender;
	}

	@Override
	public boolean supports(AuthenticationMethod method, String provider) {
		return method == AuthenticationMethod.EMAIL && "smtp".equalsIgnoreCase(provider);
	}

	@Override
	public void send(String destination, String code) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(sender);
		message.setTo(destination);
		message.setSubject("Your Client Fidelity authentication code");
		message.setText("Your authentication code is: " + code);
		mailSender.send(message);
	}
}
