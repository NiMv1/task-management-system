package com.taskmanager.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Контроллер для fallback ответов при недоступности сервисов
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", "Сервис аутентификации временно недоступен. Попробуйте позже.",
                        "errorCode", 503
                ));
    }

    @GetMapping("/task")
    public ResponseEntity<Map<String, Object>> taskFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", "Сервис задач временно недоступен. Попробуйте позже.",
                        "errorCode", 503
                ));
    }
}
