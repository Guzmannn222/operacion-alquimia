package alquimia.domain.exception;

/** The message fragments cannot be combined into one message. */
public class InconsistentMessageException extends DomainException {

	public InconsistentMessageException(String message) {
		super(message);
	}
}
