package com.taskmanager.task.dto;

import com.taskmanager.task.entity.TaskPriority;
import com.taskmanager.task.entity.TaskStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для обновления задачи
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {

    @Size(min = 3, max = 200, message = "Название должно быть от 3 до 200 символов")
    private String title;

    @Size(max = 2000, message = "Описание не должно превышать 2000 символов")
    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private Long assigneeId;

    private LocalDateTime deadline;

    private Integer estimatedHours;

    private Integer actualHours;
}
