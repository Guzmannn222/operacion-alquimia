package alquimia.domain.exception;

/** The device is not one of the known surveillance devices. */
public class UnknownDeviceException extends DomainException {

	public UnknownDeviceException(String message) {
		super(message);
	}
}
