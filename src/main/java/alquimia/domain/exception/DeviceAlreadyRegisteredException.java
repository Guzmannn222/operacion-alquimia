package alquimia.domain.exception;

/** The device already has information registered; replace or update it instead. */
public class DeviceAlreadyRegisteredException extends DomainException {

	public DeviceAlreadyRegisteredException(String message) {
		super(message);
	}
}
