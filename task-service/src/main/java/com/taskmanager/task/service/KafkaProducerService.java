package com.taskmanager.task.service;

import com.taskmanager.common.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Сервис для отправки сообщений в Kafka
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.notification-events}")
    private String notificationTopic;

    @Value("${kafka.topics.task-events}")
    private String taskEventsTopic;

    /**
     * Отправка уведомления в Kafka
     */
    public void sendNotification(NotificationDto notification) {
        CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(notificationTopic, notification.getId(), notification);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Уведомление отправлено в Kafka: topic={}, key={}, offset={}",
                        notificationTopic, notification.getId(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Ошибка отправки уведомления в Kafka: {}", ex.getMessage());
            }
        });
    }

    /**
     * Отправка события задачи в Kafka
     */
    public void sendTaskEvent(String eventType, Object payload) {
        String key = eventType + "-" + System.currentTimeMillis();
        
        CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(taskEventsTopic, key, payload);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Событие задачи отправлено в Kafka: topic={}, key={}, offset={}",
                        taskEventsTopic, key,
                        result.getRecordMetadata().offset());
            } else {
                log.error("Ошибка отправки события задачи в Kafka: {}", ex.getMessage());
            }
        });
    }
}
