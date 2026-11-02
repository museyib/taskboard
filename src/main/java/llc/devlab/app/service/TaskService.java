package llc.devlab.app.service;

import jakarta.transaction.Transactional;
import llc.devlab.app.exception.ResourceNotFoundException;
import llc.devlab.app.exception.TaskBlockedException;
import llc.devlab.app.mapper.CsvFileMapper;
import llc.devlab.app.mapper.LabelMapper;
import llc.devlab.app.mapper.TaskMapper;
import llc.devlab.app.model.dto.*;
import llc.devlab.app.model.entity.Task;
import llc.devlab.app.model.enums.TaskStatus;
import llc.devlab.app.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final LabelMapper labelMapper;
    private final CsvFileMapper csvFileMapper;

    public List<TaskDto> getUserTasks(String authenticatedEmail) {
        return taskMapper.toDtoList(taskRepository.findAllByCreatedByAndDeletedFalse(authenticatedEmail));
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

        if (request.status().equals(TaskStatus.BLOCKED)) {
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
}
