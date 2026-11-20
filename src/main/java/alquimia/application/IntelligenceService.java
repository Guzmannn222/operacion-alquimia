package alquimia.application;

import alquimia.domain.exception.DeviceAlreadyRegisteredException;
import alquimia.domain.exception.DeviceNotRegisteredException;
import alquimia.domain.exception.InsufficientDataException;
import alquimia.domain.exception.InvalidReadingException;
import alquimia.domain.model.IntelligenceResult;
import alquimia.domain.model.MessageFragment;
import alquimia.domain.model.Reading;
import alquimia.domain.model.SurveillanceDevice;
import alquimia.domain.port.in.DeleteReadingUseCase;
import alquimia.domain.port.in.ProcessIntelligenceUseCase;
import alquimia.domain.port.in.QueryIntelligenceUseCase;
import alquimia.domain.port.in.RegisterReadingUseCase;
import alquimia.domain.port.in.ReplaceReadingUseCase;
import alquimia.domain.port.in.UpdateReadingUseCase;
import alquimia.domain.port.out.ReadingRepositoryPort;
import alquimia.domain.service.MessageReconstructionService;
import alquimia.domain.service.TrilaterationService;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Application service: orchestrates the use cases. The rules live in the domain; this class only coordinates
 * the output port and the domain services. Methods are synchronized so that read-modify-write operations keep
 * the stored information consistent.
 */
@Service
public class IntelligenceService implements ProcessIntelligenceUseCase, RegisterReadingUseCase,
		QueryIntelligenceUseCase, ReplaceReadingUseCase, UpdateReadingUseCase, DeleteReadingUseCase {

	private final ReadingRepositoryPort repository;
	private final TrilaterationService trilateration = new TrilaterationService();
	private final MessageReconstructionService reconstruction = new MessageReconstructionService();

	public IntelligenceService(ReadingRepositoryPort repository) {
		this.repository = repository;
	}

	@Override
	public IntelligenceResult process(List<Reading> readings) {
		if (readings == null || readings.size() != SurveillanceDevice.values().length) {
			throw new InvalidReadingException("The information of the three devices is required.");
		}
		Map<SurveillanceDevice, Reading> byDevice = new EnumMap<>(SurveillanceDevice.class);
		for (Reading reading : readings) {
			if (reading == null || byDevice.put(reading.device(), reading) != null) {
				throw new InvalidReadingException("Each of the three devices must appear exactly once.");
			}
		}
		return compute(byDevice);
	}

	@Override
	public synchronized Reading register(String deviceName, Double distance, List<String> message) {
		SurveillanceDevice device = SurveillanceDevice.from(deviceName);
		Reading reading = new Reading(device, distance, newFragment(message));
		if (repository.findByDevice(device).isPresent()) {
			throw new DeviceAlreadyRegisteredException("The device '" + device.id()
					+ "' already has information registered; replace or update it instead.");
		}
		repository.save(reading);
		return reading;
	}

	@Override
	public synchronized IntelligenceResult query() {
		Map<SurveillanceDevice, Reading> byDevice = new EnumMap<>(SurveillanceDevice.class);
		for (Reading reading : repository.findAll()) {
			byDevice.put(reading.device(), reading);
		}
		if (byDevice.size() < SurveillanceDevice.values().length) {
			List<String> missing = new ArrayList<>();
			for (SurveillanceDevice device : SurveillanceDevice.values()) {
				if (!byDevice.containsKey(device)) {
					missing.add(device.id());
				}
			}
			throw new InsufficientDataException("Not enough data: missing information from " + missing + ".");
		}
		return compute(byDevice);
	}

	@Override
	public synchronized Reading findDevice(String deviceName) {
		return existing(SurveillanceDevice.from(deviceName));
	}

	@Override
	public synchronized List<Reading> listDevices() {
		return repository.findAll();
	}

	@Override
	public synchronized Reading replace(String deviceName, Double distance, List<String> message) {
		SurveillanceDevice device = SurveillanceDevice.from(deviceName);
		existing(device);
		Reading reading = new Reading(device, distance, newFragment(message));
		repository.save(reading);
		return reading;
	}

	@Override
	public synchronized Reading update(String deviceName, Double distance, List<String> message) {
		SurveillanceDevice device = SurveillanceDevice.from(deviceName);
		Reading current = existing(device);
		if (distance == null && message == null) {
			throw new InvalidReadingException("Provide the distance and/or the message to update.");
		}
		Reading updated = current.updated(distance, message == null ? null : new MessageFragment(message));
		repository.save(updated);
		return updated;
	}

	@Override
	public synchronized void delete(String deviceName) {
		SurveillanceDevice device = SurveillanceDevice.from(deviceName);
		existing(device);
		repository.deleteByDevice(device);
	}

	private Reading existing(SurveillanceDevice device) {
		return repository.findByDevice(device).orElseThrow(() -> new DeviceNotRegisteredException(
				"There is no information registered for the device '" + device.id() + "'."));
	}

	private MessageFragment newFragment(List<String> message) {
		return new MessageFragment(message);
	}

	private IntelligenceResult compute(Map<SurveillanceDevice, Reading> byDevice) {
		List<MessageFragment> fragments = new ArrayList<>();
		for (SurveillanceDevice device : SurveillanceDevice.values()) {
			fragments.add(byDevice.get(device).fragment());
		}
		return new IntelligenceResult(trilateration.locate(byDevice), reconstruction.reconstruct(fragments));
	}
}
