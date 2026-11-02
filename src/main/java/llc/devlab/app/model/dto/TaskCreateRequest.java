package llc.devlab.app.model.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record TaskCreateRequest(@NotBlank String name,
                                String description,
                                String deadLine,
                                List<Long> labels) {
}
