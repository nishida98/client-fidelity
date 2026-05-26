package com.lhn.client_fidelity.infrastructure.authentication.token;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.lhn.client_fidelity.application.authentication.AccessTokenVerifier;
import com.lhn.client_fidelity.application.authentication.AuthenticatedUser;
import com.lhn.client_fidelity.application.authentication.AuthenticationProperties;
import com.lhn.client_fidelity.application.user.UserRepository;
import com.lhn.client_fidelity.domain.user.User;
import com.lhn.client_fidelity.domain.user.UserId;
import com.lhn.client_fidelity.domain.user.UserType;
import com.lhn.client_fidelity.exception.InvalidAccessTokenException;
import com.lhn.client_fidelity.exception.MissingAccessTokenException;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.Base64;

@Component
public class JwtAccessTokenVerifier implements AccessTokenVerifier {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final AuthenticationProperties properties;
	private final UserRepository userRepository;
	private final Clock clock;

	public JwtAccessTokenVerifier(
			AuthenticationProperties properties,
			UserRepository userRepository,
			Clock clock
	) {
		this.properties = properties;
		this.userRepository = userRepository;
		this.clock = clock;
	}

	@Override
	public AuthenticatedUser verify(String authorizationHeader) {
		if (authorizationHeader == null || authorizationHeader.isBlank()) {
			throw new MissingAccessTokenException();
		}
		String[] segments = authorizationHeader.trim().split("\\s+");
		if (segments.length != 2 || !"Bearer".equals(segments[0])) {
			throw new InvalidAccessTokenException();
		}
		String token = segments[1];
		String[] tokenParts = token.split("\\.");
		if (tokenParts.length != 3) {
			throw new InvalidAccessTokenException();
		}
		try {
			String unsignedToken = tokenParts[0] + "." + tokenParts[1];
			String expectedSignature = sign(unsignedToken);
			if (!MessageDigest.isEqual(
					expectedSignature.getBytes(StandardCharsets.UTF_8),
					tokenParts[2].getBytes(StandardCharsets.UTF_8)
			)) {
				throw new InvalidAccessTokenException();
			}

			JsonNode payload = OBJECT_MAPPER.readTree(
					Base64.getUrlDecoder().decode(tokenParts[1].getBytes(StandardCharsets.UTF_8))
			);
			String subject = readSubject(payload);
			UserType userType = readUserType(payload);
			long expiresAt = readExpiration(payload);
			if (expiresAt <= clock.instant().getEpochSecond()) {
				throw new InvalidAccessTokenException();
			}

			User user = userRepository.findById(new UserId(subject))
					.orElseThrow(InvalidAccessTokenException::new);
			if (user.type() != userType) {
				throw new InvalidAccessTokenException();
			}
			return new AuthenticatedUser(user.id(), user.type());
		}
		catch (InvalidAccessTokenException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new InvalidAccessTokenException(exception);
		}
	}

	private String sign(String unsignedToken) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(properties.jwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		return Base64.getUrlEncoder().withoutPadding()
				.encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
	}

	private String readSubject(JsonNode payload) {
		JsonNode subjectNode = payload.get("sub");
		if (subjectNode == null || subjectNode.asText().isBlank()) {
			throw new InvalidAccessTokenException();
		}
		return subjectNode.asText().trim();
	}

	private UserType readUserType(JsonNode payload) {
		JsonNode userTypeNode = payload.get("userType");
		if (userTypeNode == null || userTypeNode.asText().isBlank()) {
			throw new InvalidAccessTokenException();
		}
		try {
			return UserType.valueOf(userTypeNode.asText().trim());
		}
		catch (IllegalArgumentException exception) {
			throw new InvalidAccessTokenException(exception);
		}
	}

	private long readExpiration(JsonNode payload) {
		JsonNode expirationNode = payload.get("exp");
		if (expirationNode == null || !expirationNode.canConvertToLong()) {
			throw new InvalidAccessTokenException();
		}
		return expirationNode.longValue();
	}
}

