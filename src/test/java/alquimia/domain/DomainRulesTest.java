package alquimia.domain;

import static org.junit.jupiter.api.Assertions.*;

import alquimia.domain.exception.*;
import alquimia.domain.model.*;
import alquimia.domain.service.*;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DomainRulesTest {

	private final TrilaterationService trilateration = new TrilaterationService();
	private final MessageReconstructionService reconstruction = new MessageReconstructionService();

	private static double dist(SurveillanceDevice d, double x, double y) {
		return Math.hypot(d.position().x() - x, d.position().y() - y);
	}

	private static Map<SurveillanceDevice, Reading> readingsFor(double x, double y) {
		Map<SurveillanceDevice, Reading> map = new EnumMap<>(SurveillanceDevice.class);
		for (SurveillanceDevice d : SurveillanceDevice.values()) {
			map.put(d, new Reading(d, dist(d, x, y), new MessageFragment(List.of("hola"))));
		}
		return map;
	}

	@Test
	@DisplayName("the three devices are at the positions given in the exam")
	void fixedPositions() {
		assertEquals(new Position(-500, -200), SurveillanceDevice.STAR_LABS.position());
		assertEquals(new Position(100, -100), SurveillanceDevice.CCPD.position());
		assertEquals(new Position(500, 100), SurveillanceDevice.STAR_SATELLITE.position());
	}

	@Test
	@DisplayName("a device is found by id or by display name, ignoring case and punctuation")
	void deviceLookup() {
		assertEquals(SurveillanceDevice.STAR_LABS, SurveillanceDevice.from("star-labs"));
		assertEquals(SurveillanceDevice.STAR_LABS, SurveillanceDevice.from("STAR Labs"));
		assertEquals(SurveillanceDevice.STAR_SATELLITE, SurveillanceDevice.from("S.T.A.R. Satellite"));
		assertEquals(SurveillanceDevice.CCPD, SurveillanceDevice.from(" ccpd "));
		assertThrows(UnknownDeviceException.class, () -> SurveillanceDevice.from("batcave"));
		assertThrows(UnknownDeviceException.class, () -> SurveillanceDevice.from(null));
		assertThrows(UnknownDeviceException.class, () -> SurveillanceDevice.from("  "));
	}

	@Test
	@DisplayName("trilateration finds the laboratory at the origin")
	void trilaterationOrigin() {
		Position p = trilateration.locate(readingsFor(0, 0));
		assertEquals(0.0, p.x(), 0.01);
		assertEquals(0.0, p.y(), 0.01);
	}

	@Test
	@DisplayName("trilateration finds other positions, including negative coordinates")
	void trilaterationOtherPoints() {
		double[][] points = { { 123.45, -67.89 }, { -300, 250 }, { 400, -150 }, { -50.5, -400.25 } };
		for (double[] point : points) {
			Position p = trilateration.locate(readingsFor(point[0], point[1]));
			assertEquals(point[0], p.x(), 0.01);
			assertEquals(point[1], p.y(), 0.01);
		}
	}

	@Test
	@DisplayName("trilateration needs the three devices")
	void trilaterationNeedsThree() {
		Map<SurveillanceDevice, Reading> two = readingsFor(0, 0);
		two.remove(SurveillanceDevice.CCPD);
		assertThrows(InsufficientDataException.class, () -> trilateration.locate(two));
	}

	@Test
	@DisplayName("the message is rebuilt ignoring empty words")
	void messageRebuilt() {
		String msg = reconstruction.reconstruct(List.of(
				new MessageFragment(List.of("la", "", "es", "", "alquimia")),
				new MessageFragment(List.of("", "piedra", "", "", "alquimia")),
				new MessageFragment(List.of("", "", "", "obra", ""))));
		assertEquals("la piedra es obra alquimia", msg);
	}

	@Test
	@DisplayName("the same word received by several devices is accepted")
	void sameWordTwice() {
		String msg = reconstruction.reconstruct(List.of(
				new MessageFragment(List.of("hola", "")),
				new MessageFragment(List.of("hola", "mundo")),
				new MessageFragment(List.of("", "mundo"))));
		assertEquals("hola mundo", msg);
	}

	@Test
	@DisplayName("a word nobody received means there is not enough information")
	void missingWord() {
		assertThrows(InsufficientDataException.class, () -> reconstruction.reconstruct(List.of(
				new MessageFragment(List.of("hola", "")),
				new MessageFragment(List.of("hola", "")),
				new MessageFragment(List.of("", "")))));
	}

	@Test
	@DisplayName("different words at the same position or different lengths are inconsistent")
	void inconsistentMessage() {
		assertThrows(InconsistentMessageException.class, () -> reconstruction.reconstruct(List.of(
				new MessageFragment(List.of("hola")),
				new MessageFragment(List.of("adios")))));
		assertThrows(InconsistentMessageException.class, () -> reconstruction.reconstruct(List.of(
				new MessageFragment(List.of("hola", "")),
				new MessageFragment(List.of("hola")))));
	}

	@Test
	@DisplayName("invalid readings are rejected")
	void invalidReadings() {
		MessageFragment ok = new MessageFragment(List.of("a"));
		SurveillanceDevice d = SurveillanceDevice.CCPD;
		assertThrows(InvalidReadingException.class, () -> new Reading(d, null, ok));
		assertThrows(InvalidReadingException.class, () -> new Reading(d, -1.0, ok));
		assertThrows(InvalidReadingException.class, () -> new Reading(d, Double.NaN, ok));
		assertThrows(InvalidReadingException.class, () -> new Reading(d, Double.POSITIVE_INFINITY, ok));
		assertThrows(InvalidReadingException.class, () -> new Reading(d, 1.0, null));
		assertThrows(InvalidReadingException.class, () -> new Reading(null, 1.0, ok));
		assertThrows(InvalidReadingException.class, () -> new MessageFragment(null));
		assertThrows(InvalidReadingException.class, () -> new MessageFragment(List.of()));
		assertThrows(InvalidReadingException.class, () -> new MessageFragment(Arrays.asList("a", null)));
		assertDoesNotThrow(() -> new Reading(d, 0.0, ok));
	}

	@Test
	@DisplayName("a partial update keeps what was not provided")
	void partialUpdate() {
		Reading r = new Reading(SurveillanceDevice.CCPD, 100.0, new MessageFragment(List.of("a", "")));
		Reading onlyDistance = r.updated(250.0, null);
		assertEquals(250.0, onlyDistance.distance(), 0.0);
		assertEquals(r.fragment(), onlyDistance.fragment());
		Reading onlyMessage = r.updated(null, new MessageFragment(List.of("", "b")));
		assertEquals(100.0, onlyMessage.distance(), 0.0);
		assertEquals(List.of("", "b"), onlyMessage.fragment().words());
	}
}
