package llc.devlab.app.security.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
    @NotBlank String token
) {}
