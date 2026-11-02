package llc.devlab.app.mapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import llc.devlab.app.model.dto.TaskCreateRequest;
import llc.devlab.app.model.dto.TaskDto;
import llc.devlab.app.model.dto.TaskUpdateRequest;
import llc.devlab.app.model.entity.Label;
import llc.devlab.app.model.entity.Task;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class TaskMapper {

    @PersistenceContext
    private EntityManager entityManager;

    public abstract TaskDto toDto(Task task);

    public abstract List<TaskDto> toDtoList(List<Task> tasks);

    public abstract Task toEntity(TaskCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateEntity(TaskUpdateRequest request, @MappingTarget Task task);

    List<Label> toLabelSet(List<Long> labels) {
        if (labels == null) {
            return null;
        }
        return labels.stream()
                .map(labelId -> entityManager.getReference(Label.class, labelId))
                .collect(Collectors.toList());
    }
}
