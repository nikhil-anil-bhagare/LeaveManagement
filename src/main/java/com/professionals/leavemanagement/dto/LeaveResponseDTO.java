package com.professionals.leavemanagement.dto;

import com.professionals.leavemanagement.model.LeaveStatus;
import com.professionals.leavemanagement.model.LeaveType;

import java.time.LocalDate;

// Good record candidate: assembled once from LeaveRequest and discarded after serialization, never mutated.
public record LeaveResponseDTO(
        Long id,
        String employeeId,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        LeaveStatus status
) {
}
