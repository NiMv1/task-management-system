package com.taskmanager.task.integration;

import com.taskmanager.task.dto.CreateProjectRequest;
import com.taskmanager.task.dto.CreateTaskRequest;
import com.taskmanager.task.entity.TaskPriority;
import com.taskmanager.task.entity.TaskStatus;
import com.taskmanager.task.repository.ProjectRepository;
import com.taskmanager.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для Task Service с Testcontainers
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class TaskIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("taskdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    @DisplayName("Полный цикл: создание проекта и задачи")
    void fullCycle_CreateProjectAndTask() throws Exception {
        // 1. Создание проекта
        String projectJson = """
            {
                "name": "Тестовый проект",
                "description": "Описание проекта"
            }
            """;

        MvcResult projectResult = mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "1")
                        .content(projectJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Тестовый проект"))
                .andReturn();

        // Извлекаем ID проекта
        String responseBody = projectResult.getResponse().getContentAsString();
        // Предполагаем что ID = 1 для первого проекта

        // 2. Создание задачи
        String taskJson = """
            {
                "title": "Первая задача",
                "description": "Описание задачи",
                "projectId": 1,
                "priority": "HIGH"
            }
            """;

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "1")
                        .content(taskJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Первая задача"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        // 3. Получение задач проекта
        mockMvc.perform(get("/api/v1/projects/1/tasks")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Обновление статуса задачи")
    void updateTaskStatus() throws Exception {
        // Создаём проект
        String projectJson = """
            {
                "name": "Проект",
                "description": "Описание"
            }
            """;

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "1")
                        .content(projectJson))
                .andExpect(status().isCreated());

        // Создаём задачу
        String taskJson = """
            {
                "title": "Задача",
                "projectId": 1,
                "priority": "MEDIUM"
            }
            """;

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "1")
                        .content(taskJson))
                .andExpect(status().isCreated());

        // Обновляем статус
        mockMvc.perform(patch("/api/v1/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "1")
                        .content("{\"status\": \"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Назначение исполнителя на задачу")
    void assignTask() throws Exception {
        // Создаём проект и задачу
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "1")
                        .content("{\"name\": \"Проект\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "1")
                        .content("{\"title\": \"Задача\", \"projectId\": 1, \"priority\": \"LOW\"}"))
                .andExpect(status().isCreated());

        // Назначаем исполнителя
        mockMvc.perform(patch("/api/v1/tasks/1/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "1")
                        .content("{\"assigneeId\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeId").value(2));
    }

    @Test
    @DisplayName("Удаление задачи")
    void deleteTask() throws Exception {
        // Создаём проект и задачу
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "1")
                        .content("{\"name\": \"Проект\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "1")
                        .content("{\"title\": \"Задача для удаления\", \"projectId\": 1, \"priority\": \"LOW\"}"))
                .andExpect(status().isCreated());

        // Удаляем задачу
        mockMvc.perform(delete("/api/v1/tasks/1")
                        .header("X-User-Id", "1"))
                .andExpect(status().isNoContent());

        // Проверяем что задача удалена
        mockMvc.perform(get("/api/v1/tasks/1")
                        .header("X-User-Id", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение задач по статусу")
    void getTasksByStatus() throws Exception {
        // Создаём проект
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "1")
                        .content("{\"name\": \"Проект\"}"))
                .andExpect(status().isCreated());

        // Создаём несколько задач
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "1")
                        .content("{\"title\": \"Задача 1\", \"projectId\": 1, \"priority\": \"HIGH\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "1")
                        .content("{\"title\": \"Задача 2\", \"projectId\": 1, \"priority\": \"LOW\"}"))
                .andExpect(status().isCreated());

        // Получаем задачи со статусом TODO
        mockMvc.perform(get("/api/v1/tasks")
                        .param("status", "TODO")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
