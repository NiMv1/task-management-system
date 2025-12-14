package com.taskmanager.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для передачи данных проекта между сервисами
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    
    private Long id;
    private String name;
    private String description;
    private String status;
    private Long ownerId;
    private List<Long> memberIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
