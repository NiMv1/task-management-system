package com.taskmanager.common.exception;

/**
 * Исключение для случаев отказа в доступе
 */
public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }
}
