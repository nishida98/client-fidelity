package com.lhn.client_fidelity.interfaces.rest.error;

import com.lhn.client_fidelity.exception.CommerceAlreadyExistsException;
import com.lhn.client_fidelity.exception.CommerceClientAlreadyExistsException;
import com.lhn.client_fidelity.exception.AuthenticationCodeAlreadySentException;
import com.lhn.client_fidelity.exception.AuthenticationDeliveryFailedException;
import com.lhn.client_fidelity.exception.AuthenticationValidationException;
import com.lhn.client_fidelity.exception.InvalidAuthenticationCodeException;
import com.lhn.client_fidelity.exception.PhoneDeliveryNotConfiguredException;
import com.lhn.client_fidelity.exception.TokenCreationException;
import com.lhn.client_fidelity.exception.UnknownAuthenticationMethodException;
import com.lhn.client_fidelity.exception.UnknownUserTypeException;
import com.lhn.client_fidelity.exception.UserCreationException;
import com.lhn.client_fidelity.exception.UserValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

	private final Clock clock;

	public RestExceptionHandler(Clock clock) {
		this.clock = clock;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException exception) {
		if (exception.getCause() instanceof UnrecognizedPropertyException unrecognizedPropertyException) {
			return ResponseEntity
					.unprocessableEntity()
					.body(new ErrorResponse(
							"VALIDATION_FAILED",
							"The request contains invalid fields.",
							List.of(new ErrorDetailResponse(
									unrecognizedPropertyException.getPropertyName(),
									"Field is not allowed."
							))
					));
		}
		return ResponseEntity
				.badRequest()
				.body(ErrorResponse.withoutDetails("MALFORMED_JSON", "Request body must be valid JSON."));
	}

	@ExceptionHandler(UnknownUserTypeException.class)
	ResponseEntity<ErrorResponse> handleUnknownUserType(UnknownUserTypeException exception) {
		return ResponseEntity
				.badRequest()
				.body(ErrorResponse.withoutDetails("UNKNOWN_USER_TYPE", exception.getMessage()));
	}

	@ExceptionHandler(UserValidationException.class)
	ResponseEntity<ErrorResponse> handleValidation(UserValidationException exception) {
		List<ErrorDetailResponse> details = exception.errors().stream()
				.map(error -> new ErrorDetailResponse(error.field(), error.message()))
				.toList();
		return ResponseEntity
				.unprocessableEntity()
				.body(new ErrorResponse("VALIDATION_FAILED", exception.getMessage(), details));
	}

	@ExceptionHandler(AuthenticationValidationException.class)
	ResponseEntity<ErrorResponse> handleAuthenticationValidation(AuthenticationValidationException exception) {
		List<ErrorDetailResponse> details = exception.errors().stream()
				.map(error -> new ErrorDetailResponse(error.field(), error.message()))
				.toList();
		return ResponseEntity
				.unprocessableEntity()
				.body(new ErrorResponse("VALIDATION_FAILED", exception.getMessage(), details));
	}

	@ExceptionHandler(UnknownAuthenticationMethodException.class)
	ResponseEntity<ErrorResponse> handleUnknownAuthenticationMethod(UnknownAuthenticationMethodException exception) {
		return ResponseEntity
				.badRequest()
				.body(ErrorResponse.withoutDetails("UNKNOWN_AUTHENTICATION_METHOD", exception.getMessage()));
	}

	@ExceptionHandler(AuthenticationCodeAlreadySentException.class)
	ResponseEntity<AuthenticationCodeAlreadySentResponse> handleCodeAlreadySent(
			AuthenticationCodeAlreadySentException exception
	) {
		long retryAfterSeconds = Math.max(0, Duration.between(clock.instant(), exception.retryAfter()).toSeconds());
		return ResponseEntity
				.status(HttpStatus.TOO_MANY_REQUESTS)
				.header("Retry-After", Long.toString(retryAfterSeconds))
				.body(new AuthenticationCodeAlreadySentResponse(
						"AUTH_CODE_ALREADY_SENT",
						exception.getMessage(),
						retryAfterSeconds
				));
	}

	@ExceptionHandler(InvalidAuthenticationCodeException.class)
	ResponseEntity<ErrorResponse> handleInvalidAuthenticationCode(InvalidAuthenticationCodeException exception) {
		return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.body(ErrorResponse.withoutDetails("INVALID_AUTHENTICATION_CODE", exception.getMessage()));
	}

	@ExceptionHandler(AuthenticationDeliveryFailedException.class)
	ResponseEntity<ErrorResponse> handleDeliveryFailure(AuthenticationDeliveryFailedException exception) {
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.withoutDetails("AUTHENTICATION_DELIVERY_FAILED", "Authentication code could not be delivered."));
	}

	@ExceptionHandler(TokenCreationException.class)
	ResponseEntity<ErrorResponse> handleTokenCreationFailure(TokenCreationException exception) {
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.withoutDetails("TOKEN_CREATION_FAILED", "Authentication token could not be created."));
	}

	@ExceptionHandler(PhoneDeliveryNotConfiguredException.class)
	ResponseEntity<ErrorResponse> handlePhoneDeliveryNotConfigured(PhoneDeliveryNotConfiguredException exception) {
		return ResponseEntity
				.status(HttpStatus.NOT_IMPLEMENTED)
				.body(ErrorResponse.withoutDetails("PHONE_DELIVERY_NOT_CONFIGURED", exception.getMessage()));
	}

	@ExceptionHandler(CommerceAlreadyExistsException.class)
	ResponseEntity<ErrorResponse> handleCommerceAlreadyExists(CommerceAlreadyExistsException exception) {
		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(ErrorResponse.withoutDetails("COMMERCE_ALREADY_EXISTS", exception.getMessage()));
	}

	@ExceptionHandler(CommerceClientAlreadyExistsException.class)
	ResponseEntity<ErrorResponse> handleCommerceClientAlreadyExists(CommerceClientAlreadyExistsException exception) {
		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(ErrorResponse.withoutDetails("COMMERCE_CLIENT_ALREADY_EXISTS", exception.getMessage()));
	}

	@ExceptionHandler(UserCreationException.class)
	ResponseEntity<ErrorResponse> handleCreationFailure(UserCreationException exception) {
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.withoutDetails("USER_CREATION_FAILED", "User could not be created."));
	}
}
