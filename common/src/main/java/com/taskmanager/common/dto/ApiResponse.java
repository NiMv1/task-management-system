package com.taskmanager.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * Стандартный формат ответа API для всех микросервисов
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String path;
    private Integer errorCode;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data, LocalDateTime timestamp, String path, Integer errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
        this.path = path;
        this.errorCode = errorCode;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getPath() { return path; }
    public Integer getErrorCode() { return errorCode; }

    // Setters
    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setData(T data) { this.data = data; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setPath(String path) { this.path = path; }
    public void setErrorCode(Integer errorCode) { this.errorCode = errorCode; }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, data, LocalDateTime.now(), null, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now(), null, null);
    }

    public static <T> ApiResponse<T> error(String message, Integer errorCode) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now(), null, errorCode);
    }

    public static <T> ApiResponse<T> error(String message, Integer errorCode, String path) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now(), path, errorCode);
    }

    // Builder pattern
    public static <T> Builder<T> builder() { return new Builder<>(); }

    public static class Builder<T> {
        private boolean success;
        private String message;
        private T data;
        private LocalDateTime timestamp;
        private String path;
        private Integer errorCode;

        public Builder<T> success(boolean success) { this.success = success; return this; }
        public Builder<T> message(String message) { this.message = message; return this; }
        public Builder<T> data(T data) { this.data = data; return this; }
        public Builder<T> timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public Builder<T> path(String path) { this.path = path; return this; }
        public Builder<T> errorCode(Integer errorCode) { this.errorCode = errorCode; return this; }

        public ApiResponse<T> build() {
            return new ApiResponse<>(success, message, data, timestamp, path, errorCode);
        }
    }
}
