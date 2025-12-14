package com.taskmanager.task.service;

import com.taskmanager.task.dto.CreateTaskRequest;
import com.taskmanager.task.dto.TaskResponse;
import com.taskmanager.task.dto.UpdateTaskRequest;
import com.taskmanager.task.entity.Task;
import com.taskmanager.task.entity.Project;
import com.taskmanager.task.entity.TaskStatus;
import com.taskmanager.task.entity.TaskPriority;
import com.taskmanager.task.repository.TaskRepository;
import com.taskmanager.task.repository.ProjectRepository;
import com.taskmanager.task.kafka.TaskEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    private TaskEventProducer taskEventProducer;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, projectRepository, taskEventProducer);
    }

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
        doNothing().when(taskEventProducer).sendTaskCreatedEvent(any(Task.class));

        // When
        TaskResponse response = taskService.createTask(request, 100L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Новая задача");
        assertThat(response.getStatus()).isEqualTo(TaskStatus.TODO);
        verify(taskRepository).save(any(Task.class));
        verify(taskEventProducer).sendTaskCreatedEvent(any(Task.class));
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
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Проект не найден");

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Обновление статуса задачи")
    void updateTaskStatus_Success() {
        // Given
        Task existingTask = Task.builder()
                .id(1L)
                .title("Задача")
                .status(TaskStatus.TODO)
                .assigneeId(100L)
                .build();

        Task updatedTask = Task.builder()
                .id(1L)
                .title("Задача")
                .status(TaskStatus.IN_PROGRESS)
                .assigneeId(100L)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        doNothing().when(taskEventProducer).sendTaskStatusChangedEvent(any(Task.class), any(TaskStatus.class));

        // When
        TaskResponse response = taskService.updateTaskStatus(1L, TaskStatus.IN_PROGRESS, 100L);

        // Then
        assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskEventProducer).sendTaskStatusChangedEvent(any(Task.class), eq(TaskStatus.TODO));
    }

    @Test
    @DisplayName("Получение задач по проекту")
    void getTasksByProject_Success() {
        // Given
        Project project = Project.builder().id(1L).name("Проект").build();
        
        List<Task> tasks = List.of(
                Task.builder().id(1L).title("Задача 1").project(project).status(TaskStatus.TODO).build(),
                Task.builder().id(2L).title("Задача 2").project(project).status(TaskStatus.IN_PROGRESS).build()
        );

        when(taskRepository.findByProjectId(1L)).thenReturn(tasks);

        // When
        List<TaskResponse> responses = taskService.getTasksByProject(1L);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getTitle()).isEqualTo("Задача 1");
    }

    @Test
    @DisplayName("Назначение исполнителя")
    void assignTask_Success() {
        // Given
        Task task = Task.builder()
                .id(1L)
                .title("Задача")
                .status(TaskStatus.TODO)
                .build();

        Task assignedTask = Task.builder()
                .id(1L)
                .title("Задача")
                .status(TaskStatus.TODO)
                .assigneeId(200L)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(assignedTask);
        doNothing().when(taskEventProducer).sendTaskAssignedEvent(any(Task.class));

        // When
        TaskResponse response = taskService.assignTask(1L, 200L);

        // Then
        assertThat(response.getAssigneeId()).isEqualTo(200L);
        verify(taskEventProducer).sendTaskAssignedEvent(any(Task.class));
    }

    @Test
    @DisplayName("Удаление задачи")
    void deleteTask_Success() {
        // Given
        Task task = Task.builder()
                .id(1L)
                .title("Задача")
                .creatorId(100L)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        doNothing().when(taskRepository).delete(task);

        // When
        taskService.deleteTask(1L, 100L);

        // Then
        verify(taskRepository).delete(task);
    }
}
