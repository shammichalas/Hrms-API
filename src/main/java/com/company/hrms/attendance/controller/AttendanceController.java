package com.company.hrms.attendance.controller;

import com.company.hrms.attendance.dto.AttendanceRequest;
import com.company.hrms.attendance.dto.AttendanceResponse;
import com.company.hrms.attendance.service.AttendanceService;
import com.company.hrms.common.dto.ApiResponse;
import com.company.hrms.employee.entity.Employee;
import com.company.hrms.employee.repository.EmployeeRepository;
import com.company.hrms.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance Management", description = "Endpoints for employee check-ins, check-outs, and manager logs adjustment")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeRepository employeeRepository;

    @PostMapping("/check-in")
    @Operation(summary = "Perform daily check-in (clock-in) for the logged-in user")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(@RequestParam(required = false) String notes) {
        String email = SecurityUtils.getCurrentUserEmail();
        AttendanceResponse response = attendanceService.checkIn(email, notes);
        return ResponseEntity.ok(ApiResponse.success("Checked in successfully", response));
    }

    @PostMapping("/check-out")
    @Operation(summary = "Perform daily check-out (clock-out) for the logged-in user")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut(@RequestParam(required = false) String notes) {
        String email = SecurityUtils.getCurrentUserEmail();
        AttendanceResponse response = attendanceService.checkOut(email, notes);
        return ResponseEntity.ok(ApiResponse.success("Checked out successfully", response));
    }

    @PostMapping("/log")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Manually record or update an attendance entry for any employee (ADMIN/HR only)")
    public ResponseEntity<ApiResponse<AttendanceResponse>> logAttendance(@Valid @RequestBody AttendanceRequest request) {
        AttendanceResponse response = attendanceService.addOrUpdateAttendance(request);
        return ResponseEntity.ok(ApiResponse.success("Attendance entry logged/updated successfully", response));
    }

    @GetMapping("/history")
    @Operation(summary = "Retrieve logged-in employee's own check-in/out history between dates")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getMyHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        String email = SecurityUtils.getCurrentUserEmail();
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee session is invalid"));

        List<AttendanceResponse> response = attendanceService.getEmployeeAttendanceHistory(employee.getId(), start, end);
        return ResponseEntity.ok(ApiResponse.success("Attendance history retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Retrieve attendance logs for all employees within a date range (ADMIN/HR only)")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getAttendanceLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) UUID departmentId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<AttendanceResponse> response = attendanceService.getAttendanceList(start, end, departmentId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Attendance logs retrieved successfully", response));
    }
}
