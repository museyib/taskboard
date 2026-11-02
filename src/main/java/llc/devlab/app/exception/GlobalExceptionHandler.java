package llc.devlab.app.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final JsonMapper jsonMapper;

    @ExceptionHandler(UserLockException.class)
    public ResponseEntity<ErrorResponse> handleException(UserLockException ex) {
        return buildResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage(), ex);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleException(ResponseStatusException ex) {
        return buildResponse(ex.getStatusCode().value(), ex.getReason(), ex);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), ex);
    }

    @ExceptionHandler(TaskBlockedException.class)
    public ResponseEntity<ErrorResponse> handleException(TaskBlockedException ex) {
        return buildResponse(HttpStatus.CONFLICT.value(), ex.getMessage(), ex);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleException(DateTimeParseException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), ex);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleException(MissingServletRequestParameterException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("Validation error occurred: {}", jsonMapper.writeValueAsString(errors), ex);
        return buildResponse(HttpStatus.BAD_REQUEST.value(), jsonMapper.writeValueAsString(errors), ex);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleException(HttpMessageNotReadableException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred", ex);
    }

    private ResponseEntity<ErrorResponse> buildResponse(int statusCode, String message, Exception e) {
        log.error("Error occurred: {}", message, e);
        ErrorResponse errorResponse = new ErrorResponse(statusCode, message);
        return ResponseEntity.status(statusCode).body(errorResponse);
    }
}
