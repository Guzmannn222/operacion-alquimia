package alquimia.domain.model;

import alquimia.domain.exception.InvalidReadingException;

/** What one device reports: its distance to the laboratory and the message fragment it intercepted. */
public final class Reading {

	private final SurveillanceDevice device;
	private final double distance;
	private final MessageFragment fragment;

	public Reading(SurveillanceDevice device, Double distance, MessageFragment fragment) {
		if (device == null) {
			throw new InvalidReadingException("The device is required.");
		}
		if (distance == null) {
			throw new InvalidReadingException("The distance to the laboratory is required.");
		}
		if (distance.isNaN() || distance.isInfinite() || distance < 0) {
			throw new InvalidReadingException("The distance must be a number greater than or equal to 0.");
		}
		if (fragment == null) {
			throw new InvalidReadingException("The message fragment is required.");
		}
		this.device = device;
		this.distance = distance;
		this.fragment = fragment;
	}

	public SurveillanceDevice device() {
		return device;
	}

	public double distance() {
		return distance;
	}

	public MessageFragment fragment() {
		return fragment;
	}

	/** Partial update: a null argument keeps the current value. */
	public Reading updated(Double newDistance, MessageFragment newFragment) {
		return new Reading(device, newDistance != null ? newDistance : distance,
				newFragment != null ? newFragment : fragment);
	}
}
