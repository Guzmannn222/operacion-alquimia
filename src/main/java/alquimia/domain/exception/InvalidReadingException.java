package alquimia.domain.exception;

/** The data received for a surveillance device is invalid or incomplete. */
public class InvalidReadingException extends DomainException {

	public InvalidReadingException(String message) {
		super(message);
	}
}
