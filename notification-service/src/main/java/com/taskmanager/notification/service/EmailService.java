package com.taskmanager.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Сервис отправки email
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Отправка простого email
     */
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            log.info("Email отправлен на: {}", to);
        } catch (Exception e) {
            log.error("Ошибка отправки email на {}: {}", to, e.getMessage());
        }
    }
}
