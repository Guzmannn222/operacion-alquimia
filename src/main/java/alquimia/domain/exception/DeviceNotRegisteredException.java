package alquimia.domain.exception;

/** There is no information registered for the device. */
public class DeviceNotRegisteredException extends DomainException {

	public DeviceNotRegisteredException(String message) {
		super(message);
	}
}
