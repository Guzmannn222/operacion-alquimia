package alquimia.application;

import static org.junit.jupiter.api.Assertions.*;

import alquimia.adapter.out.memory.InMemoryReadingRepository;
import alquimia.domain.exception.*;
import alquimia.domain.model.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IntelligenceServiceTest {

	// laboratory at (0, 0)
	private static final double D_LABS = Math.hypot(500, 200);
	private static final double D_CCPD = Math.hypot(100, 100);
	private static final double D_SAT = Math.hypot(500, 100);

	private IntelligenceService service;

	@BeforeEach
	void setUp() {
		service = new IntelligenceService(new InMemoryReadingRepository());
	}

	private void registerAll() {
		service.register("star-labs", D_LABS, List.of("la", "", "es", "", "alquimia"));
		service.register("ccpd", D_CCPD, List.of("", "piedra", "", "", "alquimia"));
		service.register("star-satellite", D_SAT, List.of("", "", "", "obra", ""));
	}

	private static Reading reading(String device, double distance, String... words) {
		return new Reading(SurveillanceDevice.from(device), distance, new MessageFragment(List.of(words)));
	}

	@Test
	@DisplayName("2.1 complete information returns position and message")
	void processComplete() {
		IntelligenceResult r = service.process(List.of(
				reading("star-labs", D_LABS, "este", "", "mensaje"),
				reading("ccpd", D_CCPD, "", "es", ""),
				reading("star-satellite", D_SAT, "", "", "")));
		assertEquals(0.0, r.position().x(), 0.01);
		assertEquals(0.0, r.position().y(), 0.01);
		assertEquals("este es mensaje", r.message());
	}

	@Test
	@DisplayName("2.1 complete information rejects missing, repeated or unknown devices")
	void processInvalid() {
		assertThrows(InvalidReadingException.class, () -> service.process(null));
		assertThrows(InvalidReadingException.class, () -> service.process(List.of(
				reading("star-labs", 1, "a"), reading("ccpd", 1, "a"))));
		assertThrows(InvalidReadingException.class, () -> service.process(List.of(
				reading("star-labs", 1, "a"), reading("star-labs", 1, "a"), reading("ccpd", 1, "a"))));
	}

	@Test
	@DisplayName("2.1 does not store anything")
	void processDoesNotStore() {
		service.process(List.of(reading("star-labs", D_LABS, "a"), reading("ccpd", D_CCPD, "a"),
				reading("star-satellite", D_SAT, "a")));
		assertTrue(service.listDevices().isEmpty());
	}

	@Test
	@DisplayName("2.2 devices can arrive one by one, in any order")
	void registerAnyOrder() {
		service.register("star-satellite", D_SAT, List.of("", "", "", "obra", ""));
		service.register("star-labs", D_LABS, List.of("la", "", "es", "", "alquimia"));
		service.register("ccpd", D_CCPD, List.of("", "piedra", "", "", "alquimia"));
		IntelligenceResult r = service.query();
		assertEquals("la piedra es obra alquimia", r.message());
		assertEquals(0.0, r.position().x(), 0.01);
		assertEquals(0.0, r.position().y(), 0.01);
		assertEquals(3, service.listDevices().size());
	}

	@Test
	@DisplayName("2.2 registering the same device twice is a conflict")
	void registerTwice() {
		service.register("ccpd", 10.0, List.of("a"));
		assertThrows(DeviceAlreadyRegisteredException.class, () -> service.register("ccpd", 20.0, List.of("b")));
		assertEquals(10.0, service.findDevice("ccpd").distance(), 0.0);
	}

	@Test
	@DisplayName("2.2 invalid or unknown devices are rejected and nothing is stored")
	void registerInvalid() {
		assertThrows(UnknownDeviceException.class, () -> service.register("batcave", 1.0, List.of("a")));
		assertThrows(InvalidReadingException.class, () -> service.register("ccpd", null, List.of("a")));
		assertThrows(InvalidReadingException.class, () -> service.register("ccpd", -5.0, List.of("a")));
		assertThrows(InvalidReadingException.class, () -> service.register("ccpd", 5.0, null));
		assertTrue(service.listDevices().isEmpty());
	}

	@Test
	@DisplayName("2.3 query without enough data says which devices are missing")
	void queryInsufficient() {
		assertThrows(InsufficientDataException.class, () -> service.query());
		service.register("ccpd", 10.0, List.of("a"));
		InsufficientDataException e = assertThrows(InsufficientDataException.class, () -> service.query());
		assertTrue(e.getMessage().contains("star-labs"));
		assertTrue(e.getMessage().contains("star-satellite"));
	}

	@Test
	@DisplayName("2.3 query of a device that was never registered")
	void findMissing() {
		assertThrows(DeviceNotRegisteredException.class, () -> service.findDevice("ccpd"));
	}

	@Test
	@DisplayName("2.4 replace changes the device completely and leaves the others alone")
	void replace() {
		registerAll();
		service.replace("ccpd", 500.0, List.of("", "", "", "", "x"));
		Reading ccpd = service.findDevice("ccpd");
		assertEquals(500.0, ccpd.distance(), 0.0);
		assertEquals(List.of("", "", "", "", "x"), ccpd.fragment().words());
		assertEquals(D_LABS, service.findDevice("star-labs").distance(), 1e-9);
		assertEquals(D_SAT, service.findDevice("star-satellite").distance(), 1e-9);
	}

	@Test
	@DisplayName("2.4 replace needs every field and an existing device")
	void replaceInvalid() {
		assertThrows(DeviceNotRegisteredException.class, () -> service.replace("ccpd", 1.0, List.of("a")));
		service.register("ccpd", 10.0, List.of("a"));
		assertThrows(InvalidReadingException.class, () -> service.replace("ccpd", null, List.of("b")));
		assertThrows(InvalidReadingException.class, () -> service.replace("ccpd", 5.0, null));
		assertEquals(10.0, service.findDevice("ccpd").distance(), 0.0);
		assertEquals(List.of("a"), service.findDevice("ccpd").fragment().words());
	}

	@Test
	@DisplayName("2.5 partial update changes only what is provided")
	void partialUpdate() {
		registerAll();
		service.update("ccpd", 300.0, null);
		assertEquals(300.0, service.findDevice("ccpd").distance(), 0.0);
		assertEquals(List.of("", "piedra", "", "", "alquimia"), service.findDevice("ccpd").fragment().words());
		service.update("ccpd", null, List.of("", "roca", "", "", "alquimia"));
		assertEquals(300.0, service.findDevice("ccpd").distance(), 0.0);
		assertEquals(List.of("", "roca", "", "", "alquimia"), service.findDevice("ccpd").fragment().words());
		assertEquals(D_LABS, service.findDevice("star-labs").distance(), 1e-9);
	}

	@Test
	@DisplayName("2.5 partial update rejects empty updates, bad values and unregistered devices")
	void partialUpdateInvalid() {
		assertThrows(DeviceNotRegisteredException.class, () -> service.update("ccpd", 1.0, null));
		service.register("ccpd", 10.0, List.of("a"));
		assertThrows(InvalidReadingException.class, () -> service.update("ccpd", null, null));
		assertThrows(InvalidReadingException.class, () -> service.update("ccpd", -1.0, null));
		assertEquals(10.0, service.findDevice("ccpd").distance(), 0.0);
	}

	@Test
	@DisplayName("2.6 delete removes only that device and the query then lacks data")
	void delete() {
		registerAll();
		service.delete("ccpd");
		assertThrows(DeviceNotRegisteredException.class, () -> service.findDevice("ccpd"));
		assertEquals(2, service.listDevices().size());
		assertThrows(InsufficientDataException.class, () -> service.query());
		service.register("ccpd", D_CCPD, List.of("", "piedra", "", "", "alquimia"));
		assertEquals("la piedra es obra alquimia", service.query().message());
	}

	@Test
	@DisplayName("2.6 deleting a device that has no information fails")
	void deleteMissing() {
		assertThrows(DeviceNotRegisteredException.class, () -> service.delete("ccpd"));
		assertThrows(UnknownDeviceException.class, () -> service.delete("batcave"));
	}

	@Test
	@DisplayName("device names are accepted by id or display name")
	void namesAccepted() {
		service.register("STAR Labs", 1.0, List.of("a"));
		assertEquals("star-labs", service.findDevice("star-labs").device().id());
		assertEquals("star-labs", service.findDevice("S.T.A.R. Labs").device().id());
	}
}
