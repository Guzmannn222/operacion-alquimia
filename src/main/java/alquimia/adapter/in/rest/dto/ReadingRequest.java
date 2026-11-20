package alquimia.adapter.in.rest.dto;

import java.util.List;

/** Body for register / replace / partial update of one device (all fields optional in the JSON). */
public record ReadingRequest(Double distance, List<String> message) {
}
