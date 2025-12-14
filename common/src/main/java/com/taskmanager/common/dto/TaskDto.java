package com.taskmanager.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для передачи данных задачи между сервисами
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Long projectId;
    private Long assigneeId;
    private Long creatorId;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
