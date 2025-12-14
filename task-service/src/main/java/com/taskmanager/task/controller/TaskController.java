package com.taskmanager.task.controller;

import com.taskmanager.common.dto.ApiResponse;
import com.taskmanager.task.dto.*;
import com.taskmanager.task.entity.TaskStatus;
import com.taskmanager.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST контроллер для управления задачами
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Задачи", description = "API для управления задачами")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Создание новой задачи")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        TaskResponse response = taskService.createTask(request, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Задача создана"));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Получение задачи по ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(@PathVariable Long taskId) {
        TaskResponse response = taskService.getTaskById(taskId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Обновление задачи")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request) {
        TaskResponse response = taskService.updateTask(taskId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Задача обновлена"));
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Удаление задачи")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok(ApiResponse.success(null, "Задача удалена"));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Получение задач проекта")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getTasksByProject(
            @PathVariable Long projectId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TaskResponse> response = taskService.getTasksByProject(projectId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/assignee/{assigneeId}")
    @Operation(summary = "Получение задач исполнителя")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getTasksByAssignee(
            @PathVariable Long assigneeId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TaskResponse> response = taskService.getTasksByAssignee(assigneeId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Получение задач по статусу")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getTasksByStatus(
            @PathVariable TaskStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TaskResponse> response = taskService.getTasksByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{taskId}/status")
    @Operation(summary = "Изменение статуса задачи")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam TaskStatus status) {
        UpdateTaskRequest request = UpdateTaskRequest.builder().status(status).build();
        TaskResponse response = taskService.updateTask(taskId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Статус задачи обновлён"));
    }

    @PatchMapping("/{taskId}/assignee")
    @Operation(summary = "Назначение исполнителя")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable Long taskId,
            @RequestParam Long assigneeId) {
        UpdateTaskRequest request = UpdateTaskRequest.builder().assigneeId(assigneeId).build();
        TaskResponse response = taskService.updateTask(taskId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Исполнитель назначен"));
    }
}
