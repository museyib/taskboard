package llc.devlab.app.model.dto;

import llc.devlab.app.model.enums.TaskStatus;

import java.util.List;

public record TaskUpdateRequest(String name,
                                String description,
                                String deadLine,
                                TaskStatus status,
                                List<Long> labels) {
}
