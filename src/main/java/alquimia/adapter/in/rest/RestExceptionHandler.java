package alquimia.adapter.in.rest;

import alquimia.adapter.in.rest.dto.ErrorResponse;
import alquimia.domain.exception.DeviceAlreadyRegisteredException;
import alquimia.domain.exception.DeviceNotRegisteredException;
import alquimia.domain.exception.InconsistentMessageException;
import alquimia.domain.exception.InsufficientDataException;
import alquimia.domain.exception.InvalidReadingException;
import alquimia.domain.exception.UnknownDeviceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Translates domain errors into HTTP status codes and a uniform JSON error body. */
@RestControllerAdvice
public class RestExceptionHandler {

	@ExceptionHandler(InvalidReadingException.class)
	public ResponseEntity<ErrorResponse> invalid(InvalidReadingException e) {
		return build(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException e) {
		return build(HttpStatus.BAD_REQUEST, "The request body is missing or is not valid JSON "
				+ "(for example, the distance must be a number and the message a list of words).");
	}

	@ExceptionHandler({ UnknownDeviceException.class, DeviceNotRegisteredException.class })
	public ResponseEntity<ErrorResponse> notFound(RuntimeException e) {
		return build(HttpStatus.NOT_FOUND, e.getMessage());
	}

	@ExceptionHandler(InsufficientDataException.class)
	public ResponseEntity<ErrorResponse> insufficient(InsufficientDataException e) {
		return build(HttpStatus.NOT_FOUND, e.getMessage());
	}

	@ExceptionHandler(DeviceAlreadyRegisteredException.class)
	public ResponseEntity<ErrorResponse> conflict(DeviceAlreadyRegisteredException e) {
		return build(HttpStatus.CONFLICT, e.getMessage());
	}

	@ExceptionHandler(InconsistentMessageException.class)
	public ResponseEntity<ErrorResponse> inconsistent(InconsistentMessageException e) {
		return build(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
	}

	private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(new ErrorResponse(status.value(), status.name(), message));
	}
}
