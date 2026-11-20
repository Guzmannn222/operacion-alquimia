package alquimia.domain.model;

/** A point (X, Y) on the Central City map. */
public record Position(double x, double y) {

	/** Same point with both coordinates rounded to two decimals (-0.0 becomes 0.0). */
	public Position rounded() {
		return new Position(round2(x), round2(y));
	}

	private static double round2(double v) {
		double r = Math.round(v * 100.0) / 100.0;
		return r == 0.0 ? 0.0 : r;
	}
}
