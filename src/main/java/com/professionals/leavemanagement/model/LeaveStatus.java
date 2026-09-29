package com.professionals.leavemanagement.model;

// New leave requests always start PENDING; the other states are only reachable
// via PATCH /leaves/{id}/status.
public enum LeaveStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}
