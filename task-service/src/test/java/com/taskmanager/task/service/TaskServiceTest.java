package com.taskmanager.task.service;

import com.taskmanager.task.dto.CreateTaskRequest;
import com.taskmanager.task.dto.TaskResponse;
import com.taskmanager.task.entity.Task;
import com.taskmanager.task.entity.Project;
import com.taskmanager.task.entity.TaskStatus;
import com.taskmanager.task.entity.TaskPriority;
import com.taskmanager.task.repository.TaskRepository;
import com.taskmanager.task.repository.ProjectRepository;
import com.taskmanager.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для TaskService
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private TaskService taskService;

    @Test
    @DisplayName("Создание задачи - успешно")
    void createTask_Success() {
        // Given
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Новая задача");
        request.setDescription("Описание задачи");
        request.setProjectId(1L);
        request.setPriority(TaskPriority.HIGH);

        Project project = Project.builder()
                .id(1L)
                .name("Тестовый проект")
                .build();

        Task savedTask = Task.builder()
                .id(1L)
                .title("Новая задача")
                .description("Описание задачи")
                .project(project)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .creatorId(100L)
                .createdAt(LocalDateTime.now())
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // When
        TaskResponse response = taskService.createTask(request, 100L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Новая задача");
        assertThat(response.getStatus()).isEqualTo(TaskStatus.TODO);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Создание задачи - ошибка: проект не найден")
    void createTask_ProjectNotFound() {
        // Given
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Задача");
        request.setProjectId(999L);

        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.createTask(request, 100L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Проверка что TaskService создаётся корректно")
    void taskService_IsNotNull() {
        // Then
        org.junit.jupiter.api.Assertions.assertNotNull(taskService);
    }
}
