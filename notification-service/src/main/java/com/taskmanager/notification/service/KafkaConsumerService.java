package com.taskmanager.notification.service;

import com.taskmanager.common.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Сервис для получения сообщений из Kafka
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${kafka.topics.notification-events}", groupId = "notification-service")
    public void consumeNotification(NotificationDto notification) {
        log.info("Получено уведомление из Kafka: id={}, type={}, userId={}",
                notification.getId(), notification.getType(), notification.getUserId());
        
        try {
            notificationService.processNotification(notification);
        } catch (Exception e) {
            log.error("Ошибка обработки уведомления: {}", e.getMessage(), e);
        }
    }
}
