package com.professionals.leavemanagement.dto;

// Stable, machine-readable identifiers for API errors; unlike the human-readable
// message, these never change wording, so clients can safely branch on them.
public enum ErrorCode {
    LEAVE_NOT_FOUND,
    INVALID_LEAVE_REQUEST,
    VALIDATION_FAILED,
    INVALID_PARAMETER,
    MALFORMED_REQUEST,
    INTERNAL_ERROR
}
