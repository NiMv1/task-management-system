package com.taskmanager.common.exception;

/**
 * Исключение для ошибок авторизации
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
}
