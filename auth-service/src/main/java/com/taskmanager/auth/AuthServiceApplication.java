package com.taskmanager.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Главный класс сервиса аутентификации
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.taskmanager.auth", "com.taskmanager.common"})
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
