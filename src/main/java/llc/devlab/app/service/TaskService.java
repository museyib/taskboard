package llc.devlab.app.service;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import llc.devlab.app.exception.ResourceNotFoundException;
import llc.devlab.app.exception.TaskBlockedException;
import llc.devlab.app.mapper.CsvFileMapper;
import llc.devlab.app.mapper.LabelMapper;
import llc.devlab.app.mapper.TaskMapper;
import llc.devlab.app.model.dto.*;
import llc.devlab.app.model.entity.Label;
import llc.devlab.app.model.entity.Task;
import llc.devlab.app.model.enums.TaskStatus;
import llc.devlab.app.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final LabelMapper labelMapper;
    private final CsvFileMapper csvFileMapper;

    public PageResponse<TaskDto> getUserTasksFiltered(String authenticatedEmail,
                                              Map<String, String> allParams,
                                              int page,
                                              int size,
                                              String sort) {
        Specification<Task> specification = buildQueryFilter(authenticatedEmail, allParams);

        Page<Task> tasks = taskRepository.findAll(specification, PageRequest.of(page, size, sort(sort)));

        List<TaskDto> content = tasks.getContent().stream().map(taskMapper::toDto).collect(Collectors.toList());

        return PageResponse.of(content, tasks);
    }

    public TaskResponse getUserTaskById(Long id, String authenticatedEmail) {
        TaskDto taskDto = taskMapper.toDto(taskRepository.findByIdAndCreatedByAndDeletedFalse(id, authenticatedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found")));

        List<LabelDto> labels = labelMapper.toDtoList(taskRepository.findLabelsByTaskId(id));

        return new TaskResponse(taskDto, labels);
    }

    @Transactional
    public TaskDto createTask(TaskCreateRequest request) {
        log.info("Creating task with request: {}", request);
        return taskMapper.toDto(taskRepository.save(taskMapper.toEntity(request)));
    }

    @Transactional
    public TaskDto updateTask(Long id, TaskUpdateRequest request, String authenticatedEmail) {
        Task task = taskRepository.findByIdAndCreatedByAndDeletedFalse(id, authenticatedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (TaskStatus.BLOCKED.equals(request.status())) {
            throw new TaskBlockedException("Cannot update task status to BLOCKED");
        }

        taskMapper.updateEntity(request, task);

        if (TaskStatus.DONE.equals(request.status())) {
            task.setClosedAt(LocalDateTime.now());
        }
        return taskMapper.toDto(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long id, String authenticatedEmail) {
        Task task = taskRepository.findByIdAndCreatedByAndDeletedFalse(id, authenticatedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        task.setDeleted(true);
        taskRepository.save(task);
    }

    public byte[] exportCompletedTasks(String authenticatedEmail) {
        List<Task> completedTasks = taskRepository.findByCreatedByAndStatusAndDeletedFalse(authenticatedEmail, TaskStatus.DONE);
        return csvFileMapper.toCsv(completedTasks);
    }

    private Specification<Task> buildQueryFilter(String email, Map<String, String> params) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("createdBy"), email));
            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));

            if (params.containsKey("status") && !params.get("status").isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), params.get("status")));
            }

            if (params.containsKey("deadLine") && !params.get("deadLine").isEmpty()) {
                LocalDate deadLine = LocalDate.parse(params.get("deadLine"));
                predicates.add(criteriaBuilder.equal(root.get("deadLine"), deadLine));
            }

            if (params.containsKey("keyword") && !params.get("keyword").isEmpty()) {
                String pattern = "%" + params.get("keyword") + "%";
                Predicate nameLike = criteriaBuilder.like(root.get("name"), pattern);
                Predicate descriptionLike = criteriaBuilder.like(root.get("description"), pattern);

                Predicate keywordMatch = criteriaBuilder.or(nameLike, descriptionLike);

                predicates.add(keywordMatch);
            }

            if (params.containsKey("label") && !params.get("label").isEmpty()) {
                Join<Task, Label> labelJoin = root.join("labels");
                predicates.add(criteriaBuilder.equal(labelJoin.get("name"), params.get("label")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort sort(String sort) {
        Sort.Direction direction = Sort.Direction.DESC;
        String sortKey = "deadLine";
        if (sort != null && !sort.isEmpty()) {
            if (sort.contains(",")) {
                String[] sortParts = sort.split(",");
                sortKey = sortParts[0];
                direction = sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            } else {
                sortKey = sort;
            }
        }
        return Sort.by(direction, sortKey);
    }
}
