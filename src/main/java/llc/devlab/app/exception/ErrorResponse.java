package llc.devlab.app.exception;

public record ErrorResponse(
        int statusCode,
        String message
) {
}
