package com.company.hrms.analytics.service;

import com.company.hrms.analytics.dto.AnalyticsStats;
import com.company.hrms.attendance.entity.Attendance;
import com.company.hrms.attendance.repository.AttendanceRepository;
import com.company.hrms.leave.entity.LeaveRequest;
import com.company.hrms.leave.entity.LeaveStatus;
import com.company.hrms.leave.repository.LeaveRepository;
import com.company.hrms.payroll.entity.Payroll;
import com.company.hrms.payroll.repository.PayrollRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final PayrollRepository payrollRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;

    @Transactional(readOnly = true)
    public AnalyticsStats getAnalytics() {
        log.info("Aggregating system analytics metrics");

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        List<Payroll> payrolls = payrollRepository.findAll();
        BigDecimal totalPayroll = payrolls.stream()
                .filter(p -> !p.getPayPeriodStart().isBefore(startOfMonth) && !p.getPayPeriodEnd().isAfter(endOfMonth))
                .map(Payroll::getNetSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Attendance> attendances = attendanceRepository.findByDateBetween(startOfMonth, endOfMonth);
        double avgHours = attendances.stream()
                .filter(a -> a.getWorkHours() != null)
                .mapToDouble(a -> a.getWorkHours().doubleValue())
                .average()
                .orElse(0.0);
        avgHours = Math.round(avgHours * 100.0) / 100.0;

        List<LeaveRequest> leaves = leaveRepository.findByStatus(LeaveStatus.APPROVED);
        long approvedLeaves = leaves.stream()
                .filter(l -> !l.getStartDate().isBefore(startOfMonth) && !l.getEndDate().isAfter(endOfMonth))
                .count();

        return AnalyticsStats.builder()
                .monthlyPayrollCost(totalPayroll)
                .averageWorkHours(avgHours)
                .leavesApprovedThisMonth(approvedLeaves)
                .build();
    }
}
