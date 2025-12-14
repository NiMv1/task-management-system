package com.taskmanager.task.dto;

import com.taskmanager.task.entity.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для создания задачи
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotBlank(message = "Название задачи обязательно")
    @Size(min = 3, max = 200, message = "Название должно быть от 3 до 200 символов")
    private String title;

    @Size(max = 2000, message = "Описание не должно превышать 2000 символов")
    private String description;

    @NotNull(message = "ID проекта обязателен")
    private Long projectId;

    private TaskPriority priority;

    private Long assigneeId;

    private LocalDateTime deadline;

    private Integer estimatedHours;
}
