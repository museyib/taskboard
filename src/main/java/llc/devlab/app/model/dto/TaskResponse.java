package llc.devlab.app.model.dto;

import java.util.List;

public record TaskResponse(TaskDto task, List<LabelDto> labels) {
}
