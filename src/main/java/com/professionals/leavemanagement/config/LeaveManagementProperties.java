package com.professionals.leavemanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "leave-management")
public class LeaveManagementProperties {

    // Default only applies if leave-management.max-leave-duration-days is absent from config;
    // application.yml's value always wins when present.
    private int maxLeaveDurationDays = 30;

    public int getMaxLeaveDurationDays() {
        return maxLeaveDurationDays;
    }

    public void setMaxLeaveDurationDays(int maxLeaveDurationDays) {
        this.maxLeaveDurationDays = maxLeaveDurationDays;
    }
}
