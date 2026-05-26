package com.lhn.client_fidelity.interfaces.rest.error;

import com.lhn.client_fidelity.exception.CommerceAlreadyExistsException;
import com.lhn.client_fidelity.exception.CommerceClientAlreadyExistsException;
import com.lhn.client_fidelity.exception.UnknownUserTypeException;
import com.lhn.client_fidelity.exception.UserCreationException;
import com.lhn.client_fidelity.exception.UserValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

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
