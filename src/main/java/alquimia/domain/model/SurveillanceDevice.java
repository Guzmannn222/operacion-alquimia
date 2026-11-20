package alquimia.domain.model;

import alquimia.domain.exception.UnknownDeviceException;
import java.util.Locale;

/** The three surveillance devices of Central City, each one at a fixed, known position. */
public enum SurveillanceDevice {

	STAR_LABS("star-labs", "STAR Labs", new Position(-500, -200)),
	CCPD("ccpd", "CCPD", new Position(100, -100)),
	STAR_SATELLITE("star-satellite", "S.T.A.R. Satellite", new Position(500, 100));

	private final String id;
	private final String displayName;
	private final Position position;

	SurveillanceDevice(String id, String displayName, Position position) {
		this.id = id;
		this.displayName = displayName;
		this.position = position;
	}

	public String id() {
		return id;
	}

	public String displayName() {
		return displayName;
	}

	public Position position() {
		return position;
	}

	/**
	 * Finds a device by its id ("star-labs") or by its display name ("STAR Labs"). Case, spaces, dots and
	 * hyphens are ignored, so "S.T.A.R. Satellite" and "star-satellite" are the same device.
	 */
	public static SurveillanceDevice from(String name) {
		if (name == null || name.isBlank()) {
			throw new UnknownDeviceException("The device name is required.");
		}
		String wanted = normalize(name);
		for (SurveillanceDevice device : values()) {
			if (normalize(device.id).equals(wanted) || normalize(device.displayName).equals(wanted)) {
				return device;
			}
		}
		throw new UnknownDeviceException("Unknown surveillance device: '" + name + "'. Known devices: "
				+ "star-labs, ccpd, star-satellite.");
	}

	private static String normalize(String value) {
		return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
	}
}
