package alquimia.domain.service;

import alquimia.domain.exception.InsufficientDataException;
import alquimia.domain.model.Position;
import alquimia.domain.model.Reading;
import alquimia.domain.model.SurveillanceDevice;
import java.util.Map;

/** Finds the laboratory by trilateration: the point whose distances to the three devices match the readings. */
public class TrilaterationService {

	public Position locate(Map<SurveillanceDevice, Reading> readings) {
		Reading a = readings.get(SurveillanceDevice.STAR_LABS);
		Reading b = readings.get(SurveillanceDevice.CCPD);
		Reading c = readings.get(SurveillanceDevice.STAR_SATELLITE);
		if (a == null || b == null || c == null) {
			throw new InsufficientDataException("The distance of the three devices is needed to locate the laboratory.");
		}
		double x1 = a.device().position().x(), y1 = a.device().position().y(), r1 = a.distance();
		double x2 = b.device().position().x(), y2 = b.device().position().y(), r2 = b.distance();
		double x3 = c.device().position().x(), y3 = c.device().position().y(), r3 = c.distance();

		// Subtracting the circle equations pairwise leaves a 2x2 linear system:  A x + B y = C  and  D x + E y = F
		double A = 2 * (x2 - x1), B = 2 * (y2 - y1);
		double C = r1 * r1 - r2 * r2 - x1 * x1 + x2 * x2 - y1 * y1 + y2 * y2;
		double D = 2 * (x3 - x2), E = 2 * (y3 - y2);
		double F = r2 * r2 - r3 * r3 - x2 * x2 + x3 * x3 - y2 * y2 + y3 * y3;

		double det = A * E - B * D;
		if (Math.abs(det) < 1e-9) {
			throw new InsufficientDataException("The three devices are aligned, so the position cannot be determined.");
		}
		double x = (C * E - F * B) / det;
		double y = (A * F - D * C) / det;
		return new Position(x, y).rounded();
	}
}
