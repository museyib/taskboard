package llc.devlab.app.model.dto;

public record TaskDto(Long id,
                      String name,
                      String description,
                      String deadLine,
                      String status) {
}
