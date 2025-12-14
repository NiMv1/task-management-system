package com.taskmanager.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Главный класс сервиса управления задачами
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.taskmanager.task", "com.taskmanager.common"})
public class TaskServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}
