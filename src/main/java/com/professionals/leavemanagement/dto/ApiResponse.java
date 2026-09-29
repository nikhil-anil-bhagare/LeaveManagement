package com.professionals.leavemanagement.dto;

import java.time.LocalDateTime;
import java.util.List;

// Good record candidate: built once via success()/error() and no code ever mutates it afterward.
public record ApiResponse<T>(boolean success, String message, T data, List<String> errors,
                              ErrorCode errorCode, LocalDateTime timestamp) {

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, null, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message, List<String> errors, ErrorCode errorCode) {
        return new ApiResponse<>(false, message, null, errors, errorCode, LocalDateTime.now());
    }
}
