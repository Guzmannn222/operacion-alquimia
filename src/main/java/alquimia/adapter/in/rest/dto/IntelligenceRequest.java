package alquimia.adapter.in.rest.dto;

import java.util.List;

/** Body of the "complete information" request: the data of the three devices. */
public record IntelligenceRequest(List<DeviceReadingRequest> devices) {
}
