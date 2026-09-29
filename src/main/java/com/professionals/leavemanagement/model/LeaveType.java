package com.professionals.leavemanagement.model;

// Every value is client-selectable via the leaveType field on incoming requests;
// none are chosen by server-side logic.
public enum LeaveType {
    SICK,
    CASUAL,
    EARNED,
    UNPAID,
    MATERNITY,
    PATERNITY
}
