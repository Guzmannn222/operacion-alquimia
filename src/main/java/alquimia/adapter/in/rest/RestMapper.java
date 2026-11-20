package alquimia.adapter.in.rest;

import alquimia.adapter.in.rest.dto.DeviceReadingRequest;
import alquimia.adapter.in.rest.dto.DeviceResponse;
import alquimia.adapter.in.rest.dto.IntelligenceResponse;
import alquimia.adapter.in.rest.dto.PositionResponse;
import alquimia.domain.exception.InvalidReadingException;
import alquimia.domain.model.IntelligenceResult;
import alquimia.domain.model.MessageFragment;
import alquimia.domain.model.Position;
import alquimia.domain.model.Reading;
import alquimia.domain.model.SurveillanceDevice;

/** Translates between the REST DTOs and the domain objects. */
final class RestMapper {

	private RestMapper() {
	}

	static Reading toReading(DeviceReadingRequest request) {
		if (request == null) {
			throw new InvalidReadingException("Each device entry is required.");
		}
		return new Reading(SurveillanceDevice.from(request.name()), request.distance(),
				new MessageFragment(request.message()));
	}

	static PositionResponse toResponse(Position position) {
		return new PositionResponse(position.x(), position.y());
	}

	static IntelligenceResponse toResponse(IntelligenceResult result) {
		return new IntelligenceResponse(toResponse(result.position()), result.message());
	}

	static DeviceResponse toResponse(Reading reading) {
		SurveillanceDevice device = reading.device();
		return new DeviceResponse(device.id(), device.displayName(), toResponse(device.position()),
				reading.distance(), reading.fragment().words());
	}
}
