package alquimia.adapter.in.rest.dto;

import java.util.List;

public record DeviceResponse(String name, String displayName, PositionResponse position, double distance,
		List<String> message) {
}
