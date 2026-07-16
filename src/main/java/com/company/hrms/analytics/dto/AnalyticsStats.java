package com.company.hrms.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsStats {
    private BigDecimal monthlyPayrollCost;
    private double averageWorkHours;
    private long leavesApprovedThisMonth;
}
