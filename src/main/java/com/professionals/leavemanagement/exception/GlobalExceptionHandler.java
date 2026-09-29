package com.professionals.leavemanagement.exception;

import com.professionals.leavemanagement.dto.ApiResponse;
import com.professionals.leavemanagement.dto.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(LeaveNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleLeaveNotFound(LeaveNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null, ex.getErrorCode());
    }

    @ExceptionHandler(InvalidLeaveRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidLeaveRequest(InvalidLeaveRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null, ex.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", details, ErrorCode.VALIDATION_FAILED);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value provided for '" + ex.getName() + "'";
        return buildResponse(HttpStatus.BAD_REQUEST, message, null, ErrorCode.INVALID_PARAMETER);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Request body is missing or malformed", null,
                ErrorCode.MALFORMED_REQUEST);
    }

    // Catch-all: log the real exception server-side, but never leak its message/stack trace to the client.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unhandled exception while processing request", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.",
                null, ErrorCode.INTERNAL_ERROR);
    }

    private ResponseEntity<ApiResponse<Object>> buildResponse(HttpStatus status, String message,
                                                                List<String> errors, ErrorCode errorCode) {
        return ResponseEntity.status(status).body(ApiResponse.error(message, errors, errorCode));
    }
}
