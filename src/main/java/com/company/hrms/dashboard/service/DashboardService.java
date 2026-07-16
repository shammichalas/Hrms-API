package com.company.hrms.dashboard.service;

import com.company.hrms.attendance.entity.Attendance;
import com.company.hrms.attendance.entity.AttendanceStatus;
import com.company.hrms.attendance.repository.AttendanceRepository;
import com.company.hrms.dashboard.dto.DashboardStats;
import com.company.hrms.department.repository.DepartmentRepository;
import com.company.hrms.employee.repository.EmployeeRepository;
import com.company.hrms.leave.entity.LeaveStatus;
import com.company.hrms.leave.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveRepository leaveRepository;
    private final AttendanceRepository attendanceRepository;

    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        log.info("Aggregating dashboard statistics");
        long totalEmployees = employeeRepository.count();
        long totalDepartments = departmentRepository.count();
        
        long pendingLeaves = leaveRepository.findByStatus(LeaveStatus.PENDING).size();

        LocalDate today = LocalDate.now();
        List<Attendance> todayAttendance = attendanceRepository.findByDateBetween(today, today);

        long presentToday = todayAttendance.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.HALF_DAY || a.getStatus() == AttendanceStatus.LATE)
                .count();

        long lateToday = todayAttendance.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();

        long absentToday = totalEmployees - presentToday;
        if (absentToday < 0) {
            absentToday = 0;
        }

        return DashboardStats.builder()
                .totalEmployees(totalEmployees)
                .totalDepartments(totalDepartments)
                .pendingLeaveRequests(pendingLeaves)
                .presentToday(presentToday)
                .absentToday(absentToday)
                .lateToday(lateToday)
                .build();
    }
}
