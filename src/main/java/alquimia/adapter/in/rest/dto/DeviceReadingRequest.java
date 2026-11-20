package alquimia.adapter.in.rest.dto;

import java.util.List;

/** One device inside the "complete information" request. */
public record DeviceReadingRequest(String name, Double distance, List<String> message) {
}
