package com.taskmanager.task.controller;

import com.taskmanager.common.dto.ApiResponse;
import com.taskmanager.task.dto.*;
import com.taskmanager.task.entity.ProjectStatus;
import com.taskmanager.task.service.ProjectService;
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
 * REST контроллер для управления проектами
 */
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Проекты", description = "API для управления проектами")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Создание нового проекта")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {
        ProjectResponse response = projectService.createProject(request, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Проект создан"));
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Получение проекта по ID")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(@PathVariable Long projectId) {
        ProjectResponse response = projectService.getProjectById(projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "Обновление проекта")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request) {
        ProjectResponse response = projectService.updateProject(projectId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Проект обновлён"));
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "Удаление проекта")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(null, "Проект удалён"));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Получение проектов пользователя")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getProjectsByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProjectResponse> response = projectService.getProjectsByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Получение проектов по статусу")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getProjectsByStatus(
            @PathVariable ProjectStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProjectResponse> response = projectService.getProjectsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{projectId}/members/{memberId}")
    @Operation(summary = "Добавление участника в проект")
    public ResponseEntity<ApiResponse<ProjectResponse>> addMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId) {
        ProjectResponse response = projectService.addMember(projectId, memberId);
        return ResponseEntity.ok(ApiResponse.success(response, "Участник добавлен"));
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    @Operation(summary = "Удаление участника из проекта")
    public ResponseEntity<ApiResponse<ProjectResponse>> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId) {
        ProjectResponse response = projectService.removeMember(projectId, memberId);
        return ResponseEntity.ok(ApiResponse.success(response, "Участник удалён"));
    }
}
