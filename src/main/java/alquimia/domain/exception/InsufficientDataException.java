package alquimia.domain.exception;

/** There is not enough information to locate the laboratory and rebuild the message. */
public class InsufficientDataException extends DomainException {

	public InsufficientDataException(String message) {
		super(message);
	}
}
