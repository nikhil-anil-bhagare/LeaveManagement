package com.professionals.leavemanagement.exception;

import com.professionals.leavemanagement.dto.ErrorCode;

public class LeaveNotFoundException extends RuntimeException {

    private final ErrorCode errorCode = ErrorCode.LEAVE_NOT_FOUND;

    public LeaveNotFoundException(Long id) {
        super("Leave request not found with id: " + id);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
