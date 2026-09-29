package com.professionals.leavemanagement.exception;

import com.professionals.leavemanagement.dto.ErrorCode;

public class InvalidLeaveRequestException extends RuntimeException {

    private final ErrorCode errorCode = ErrorCode.INVALID_LEAVE_REQUEST;

    public InvalidLeaveRequestException(String message) {
        super(message);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
