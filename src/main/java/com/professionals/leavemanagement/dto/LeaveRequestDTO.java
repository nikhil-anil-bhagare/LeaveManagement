package com.professionals.leavemanagement.dto;

import com.professionals.leavemanagement.model.LeaveType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// Good record candidate: deserialized once by Jackson, validated, then only ever read.
public record LeaveRequestDTO(

        @NotBlank(message = "employeeId is required")
        String employeeId,

        @NotNull(message = "leaveType is required")
        LeaveType leaveType,

        @NotNull(message = "startDate is required")
        @FutureOrPresent(message = "startDate must be today or in the future")
        LocalDate startDate,

        @NotNull(message = "endDate is required")
        LocalDate endDate,

        @NotBlank(message = "reason is required")
        @Size(max = 500, message = "reason must not exceed 500 characters")
        String reason
) {
}
