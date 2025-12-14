package com.taskmanager.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для передачи уведомлений через Kafka
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    
    private String id;
    private Long userId;
    private String type;
    private String title;
    private String message;
    private String channel; // EMAIL, PUSH, SMS
    private boolean read;
    private LocalDateTime createdAt;
}
