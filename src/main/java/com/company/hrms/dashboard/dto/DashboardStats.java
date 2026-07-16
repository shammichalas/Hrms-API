package com.company.hrms.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStats {
    private long totalEmployees;
    private long totalDepartments;
    private long pendingLeaveRequests;
    private long presentToday;
    private long absentToday;
    private long lateToday;
}
