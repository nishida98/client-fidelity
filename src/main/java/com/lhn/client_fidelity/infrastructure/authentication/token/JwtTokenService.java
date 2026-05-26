package com.lhn.client_fidelity.infrastructure.authentication.token;

import com.lhn.client_fidelity.application.authentication.AuthenticationProperties;
import com.lhn.client_fidelity.application.authentication.TokenResult;
import com.lhn.client_fidelity.application.authentication.TokenService;
import com.lhn.client_fidelity.domain.user.User;
import com.lhn.client_fidelity.exception.TokenCreationException;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Component
class JwtTokenService implements TokenService {

	private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();

	private final AuthenticationProperties properties;

	JwtTokenService(AuthenticationProperties properties) {
		this.properties = properties;
	}

	@Override
	public TokenResult createToken(User user, Instant issuedAt) {
		try {
			Instant expiresAt = issuedAt.plus(properties.jwtExpiration());
			String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
			String payload = encode("""
					{"sub":"%s","userType":"%s","iat":%d,"exp":%d}
					""".formatted(
					escape(user.id().value()),
					user.type().name(),
					issuedAt.getEpochSecond(),
					expiresAt.getEpochSecond()
			).trim());
			String unsignedToken = header + "." + payload;
			String signature = sign(unsignedToken);
			return new TokenResult("Bearer", unsignedToken + "." + signature, expiresAt);
		}
		catch (RuntimeException exception) {
			throw new TokenCreationException(exception);
		}
	}

	private String encode(String value) {
		return BASE64_URL.encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	private String sign(String unsignedToken) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(properties.jwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return BASE64_URL.encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
		}
		catch (Exception exception) {
			throw new IllegalStateException("Could not sign JWT.", exception);
		}
	}

	private String escape(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
