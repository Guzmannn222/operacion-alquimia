package alquimia.adapter.in.rest.dto;

public record ErrorResponse(int status, String error, String message) {
}
