package com.professionals.leavemanagement.dto;

import com.professionals.leavemanagement.model.LeaveStatus;
import jakarta.validation.constraints.NotNull;

// Good record candidate: deserialized once by Jackson, validated, then only ever read.
public record UpdateLeaveStatusDTO(
        @NotNull(message = "status is required")
        LeaveStatus status
) {
}
