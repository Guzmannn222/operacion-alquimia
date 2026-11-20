package alquimia.adapter.in.rest;

import alquimia.adapter.in.rest.dto.DeviceResponse;
import alquimia.adapter.in.rest.dto.IntelligenceRequest;
import alquimia.adapter.in.rest.dto.IntelligenceResponse;
import alquimia.adapter.in.rest.dto.ReadingRequest;
import alquimia.domain.exception.InvalidReadingException;
import alquimia.domain.model.Reading;
import alquimia.domain.port.in.DeleteReadingUseCase;
import alquimia.domain.port.in.ProcessIntelligenceUseCase;
import alquimia.domain.port.in.QueryIntelligenceUseCase;
import alquimia.domain.port.in.RegisterReadingUseCase;
import alquimia.domain.port.in.ReplaceReadingUseCase;
import alquimia.domain.port.in.UpdateReadingUseCase;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Input adapter (HTTP). It only translates HTTP into use-case calls; the domain knows nothing about it. */
@RestController
@RequestMapping("/api/v1")
public class IntelligenceController {

	private final ProcessIntelligenceUseCase processUseCase;
	private final RegisterReadingUseCase registerUseCase;
	private final QueryIntelligenceUseCase queryUseCase;
	private final ReplaceReadingUseCase replaceUseCase;
	private final UpdateReadingUseCase updateUseCase;
	private final DeleteReadingUseCase deleteUseCase;

	public IntelligenceController(ProcessIntelligenceUseCase processUseCase, RegisterReadingUseCase registerUseCase,
			QueryIntelligenceUseCase queryUseCase, ReplaceReadingUseCase replaceUseCase,
			UpdateReadingUseCase updateUseCase, DeleteReadingUseCase deleteUseCase) {
		this.processUseCase = processUseCase;
		this.registerUseCase = registerUseCase;
		this.queryUseCase = queryUseCase;
		this.replaceUseCase = replaceUseCase;
		this.updateUseCase = updateUseCase;
		this.deleteUseCase = deleteUseCase;
	}

	/** 2.1 Information of the three devices at once. */
	@PostMapping("/intelligence")
	public IntelligenceResponse process(@RequestBody IntelligenceRequest request) {
		if (request == null || request.devices() == null) {
			throw new InvalidReadingException("The 'devices' list is required.");
		}
		List<Reading> readings = new ArrayList<>();
		request.devices().forEach(device -> readings.add(RestMapper.toReading(device)));
		return RestMapper.toResponse(processUseCase.process(readings));
	}

	/** 2.3 Position and message from the accumulated information. */
	@GetMapping("/intelligence")
	public IntelligenceResponse query() {
		return RestMapper.toResponse(queryUseCase.query());
	}

	@GetMapping("/devices")
	public List<DeviceResponse> list() {
		return queryUseCase.listDevices().stream().map(RestMapper::toResponse).toList();
	}

	@GetMapping("/devices/{name}")
	public DeviceResponse find(@PathVariable("name") String name) {
		return RestMapper.toResponse(queryUseCase.findDevice(name));
	}

	/** 2.2 Register one device. */
	@PostMapping("/devices/{name}")
	@ResponseStatus(HttpStatus.CREATED)
	public DeviceResponse register(@PathVariable("name") String name, @RequestBody ReadingRequest request) {
		ReadingRequest body = required(request);
		return RestMapper.toResponse(registerUseCase.register(name, body.distance(), body.message()));
	}

	/** 2.4 Replace all the data of one device. */
	@PutMapping("/devices/{name}")
	public DeviceResponse replace(@PathVariable("name") String name, @RequestBody ReadingRequest request) {
		ReadingRequest body = required(request);
		return RestMapper.toResponse(replaceUseCase.replace(name, body.distance(), body.message()));
	}

	/** 2.5 Partial update of one device. */
	@PatchMapping("/devices/{name}")
	public DeviceResponse update(@PathVariable("name") String name, @RequestBody ReadingRequest request) {
		ReadingRequest body = required(request);
		return RestMapper.toResponse(updateUseCase.update(name, body.distance(), body.message()));
	}

	/** 2.6 Delete one device. */
	@DeleteMapping("/devices/{name}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("name") String name) {
		deleteUseCase.delete(name);
	}

	private ReadingRequest required(ReadingRequest request) {
		if (request == null) {
			throw new InvalidReadingException("The request body is required.");
		}
		return request;
	}
}
