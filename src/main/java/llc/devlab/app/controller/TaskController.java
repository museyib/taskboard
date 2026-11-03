package llc.devlab.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import llc.devlab.app.model.dto.*;
import llc.devlab.app.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @Operation(
            description = "Get user tasks",
            summary = "Get user tasks",
            tags = {"Task"}
    )
    @GetMapping
    public ResponseEntity<List<TaskDto>> getUserTasks(@AuthenticationPrincipal UserDetails userDetails,
                                                      @RequestParam(required = false) Map<String, String> allParams,
                                                      @RequestParam int page,
                                                      @RequestParam int size,
                                                      @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(taskService.getUserTasksFiltered(userDetails.getUsername(), allParams, page, size, sort));
    }

    @Operation(
            description = "Get task by id",
            summary = "Get task by id",
            tags = {"Task"}
    )
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getUserTaskById(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.getUserTaskById(id, userDetails.getUsername()));
    }

    @Operation(
            description = "Create task",
            summary = "Create task",
            tags = {"Task"}
    )
    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskCreateRequest request) {
        return ResponseEntity.ok(taskService.createTask(request));
    }

    @Operation(
            description = "Update task",
            summary = "Update task",
            tags = {"Task"}
    )
    @PatchMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long id,
                                              @RequestBody TaskUpdateRequest request,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.updateTask(id, request, userDetails.getUsername()));
    }

    @Operation(
            description = "Delete task",
            summary = "Delete task",
            tags = {"Task"}
    )
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails) {
        taskService.deleteTask(id, userDetails.getUsername());
    }

    @Operation(
            description = "Export completed tasks",
            summary = "Export completed tasks",
            tags = {"Task"}
    )
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCompletedTasks(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.exportCompletedTasks(userDetails.getUsername()));
    }
}
