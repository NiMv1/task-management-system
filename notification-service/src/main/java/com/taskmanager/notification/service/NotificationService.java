package com.taskmanager.notification.service;

import com.taskmanager.common.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Сервис обработки уведомлений
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;

    /**
     * Обработка уведомления
     */
    public void processNotification(NotificationDto notification) {
        log.info("Обработка уведомления: {} для пользователя {}", 
                notification.getType(), notification.getUserId());

        switch (notification.getChannel()) {
            case "EMAIL" -> sendEmailNotification(notification);
            case "PUSH" -> sendPushNotification(notification);
            case "SMS" -> sendSmsNotification(notification);
            default -> log.warn("Неизвестный канал уведомления: {}", notification.getChannel());
        }
    }

    private void sendEmailNotification(NotificationDto notification) {
        log.info("Отправка email уведомления пользователю: {}", notification.getUserId());
        // В реальном приложении здесь был бы запрос к auth-service для получения email
        // emailService.sendEmail(userEmail, notification.getTitle(), notification.getMessage());
        log.info("Email уведомление отправлено (симуляция)");
    }

    private void sendPushNotification(NotificationDto notification) {
        log.info("Отправка push уведомления пользователю: {}", notification.getUserId());
        // Интеграция с push-сервисом (Firebase, OneSignal и т.д.)
        log.info("Push уведомление отправлено (симуляция)");
    }

    private void sendSmsNotification(NotificationDto notification) {
        log.info("Отправка SMS уведомления пользователю: {}", notification.getUserId());
        // Интеграция с SMS-провайдером (Twilio, SMS.ru и т.д.)
        log.info("SMS уведомление отправлено (симуляция)");
    }
}
