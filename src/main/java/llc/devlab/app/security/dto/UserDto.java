package llc.devlab.app.security.dto;

public record UserDto(
    Long id,
    String email,
    String name,
    String role,
    String status
) {}
