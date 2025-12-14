package com.taskmanager.task.service;

import com.taskmanager.common.dto.NotificationDto;
import com.taskmanager.common.exception.ResourceNotFoundException;
import com.taskmanager.task.dto.*;
import com.taskmanager.task.entity.Project;
import com.taskmanager.task.entity.Task;
import com.taskmanager.task.entity.TaskPriority;
import com.taskmanager.task.entity.TaskStatus;
import com.taskmanager.task.repository.ProjectRepository;
import com.taskmanager.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сервис для управления задачами
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final KafkaProducerService kafkaProducerService;

    /**
     * Создание новой задачи
     */
    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponse createTask(CreateTaskRequest request, Long creatorId) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Проект", "id", request.getProjectId()));

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .project(project)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .assigneeId(request.getAssigneeId())
                .creatorId(creatorId)
                .deadline(request.getDeadline())
                .estimatedHours(request.getEstimatedHours())
                .build();

        task = taskRepository.save(task);
        log.info("Создана задача: {} в проекте: {}", task.getTitle(), project.getName());

        // Отправка уведомления если назначен исполнитель
        if (task.getAssigneeId() != null) {
            sendTaskAssignedNotification(task);
        }

        return TaskResponse.fromEntity(task);
    }

    /**
     * Получение задачи по ID
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#taskId")
    public TaskResponse getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача", "id", taskId));
        return TaskResponse.fromEntity(task);
    }

    /**
     * Обновление задачи
     */
    @Transactional
    @CacheEvict(value = "tasks", key = "#taskId")
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача", "id", taskId));

        Long previousAssignee = task.getAssigneeId();
        TaskStatus previousStatus = task.getStatus();

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getAssigneeId() != null) {
            task.setAssigneeId(request.getAssigneeId());
        }
        if (request.getDeadline() != null) {
            task.setDeadline(request.getDeadline());
        }
        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getActualHours() != null) {
            task.setActualHours(request.getActualHours());
        }

        task = taskRepository.save(task);
        log.info("Обновлена задача: {}", task.getId());

        // Уведомление о смене исполнителя
        if (request.getAssigneeId() != null && !request.getAssigneeId().equals(previousAssignee)) {
            sendTaskAssignedNotification(task);
        }

        // Уведомление о смене статуса
        if (request.getStatus() != null && request.getStatus() != previousStatus) {
            sendTaskStatusChangedNotification(task, previousStatus);
        }

        return TaskResponse.fromEntity(task);
    }

    /**
     * Удаление задачи
     */
    @Transactional
    @CacheEvict(value = "tasks", key = "#taskId")
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача", "id", taskId));
        taskRepository.delete(task);
        log.info("Удалена задача: {}", taskId);
    }

    /**
     * Получение задач проекта
     */
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByProject(Long projectId, Pageable pageable) {
        return taskRepository.findByProjectId(projectId, pageable)
                .map(TaskResponse::fromEntity);
    }

    /**
     * Получение задач исполнителя
     */
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByAssignee(Long assigneeId, Pageable pageable) {
        return taskRepository.findByAssigneeId(assigneeId, pageable)
                .map(TaskResponse::fromEntity);
    }

    /**
     * Получение задач по статусу
     */
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByStatus(TaskStatus status, Pageable pageable) {
        return taskRepository.findByStatus(status, pageable)
                .map(TaskResponse::fromEntity);
    }

    private void sendTaskAssignedNotification(Task task) {
        NotificationDto notification = NotificationDto.builder()
                .id(UUID.randomUUID().toString())
                .userId(task.getAssigneeId())
                .type("TASK_ASSIGNED")
                .title("Новая задача")
                .message("Вам назначена задача: " + task.getTitle())
                .channel("EMAIL")
                .createdAt(LocalDateTime.now())
                .build();
        kafkaProducerService.sendNotification(notification);
    }

    private void sendTaskStatusChangedNotification(Task task, TaskStatus previousStatus) {
        NotificationDto notification = NotificationDto.builder()
                .id(UUID.randomUUID().toString())
                .userId(task.getCreatorId())
                .type("TASK_STATUS_CHANGED")
                .title("Статус задачи изменён")
                .message(String.format("Задача '%s' изменила статус с %s на %s",
                        task.getTitle(), previousStatus, task.getStatus()))
                .channel("EMAIL")
                .createdAt(LocalDateTime.now())
                .build();
        kafkaProducerService.sendNotification(notification);
    }
}
