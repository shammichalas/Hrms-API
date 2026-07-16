package com.company.hrms.attendance.service;

import com.company.hrms.attendance.dto.AttendanceRequest;
import com.company.hrms.attendance.dto.AttendanceResponse;
import com.company.hrms.attendance.entity.Attendance;
import com.company.hrms.attendance.entity.AttendanceStatus;
import com.company.hrms.attendance.mapper.AttendanceMapper;
import com.company.hrms.attendance.repository.AttendanceRepository;
import com.company.hrms.common.exception.BadRequestException;
import com.company.hrms.common.exception.ResourceNotFoundException;
import com.company.hrms.employee.entity.Employee;
import com.company.hrms.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceMapper attendanceMapper;

    @Transactional
    public AttendanceResponse checkIn(String email, String notes) {
        log.info("Check-in attempt for email: {}", email);
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));

        LocalDate today = LocalDate.now();
        if (attendanceRepository.existsByEmployeeIdAndDate(employee.getId(), today)) {
            throw new BadRequestException("Employee already checked in for today: " + today);
        }

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(today);
        attendance.setCheckIn(LocalDateTime.now());
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance.setNotes(notes);

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Check-in successful for employee: {}, ID: {}", email, saved.getId());
        return attendanceMapper.toResponse(saved);
    }

    @Transactional
    public AttendanceResponse checkOut(String email, String notes) {
        log.info("Check-out attempt for email: {}", email);
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), today)
                .orElseThrow(() -> new BadRequestException("No check-in record found for today: " + today));

        if (attendance.getCheckOut() != null) {
            throw new BadRequestException("Employee already checked out for today.");
        }

        LocalDateTime checkOutTime = LocalDateTime.now();
        attendance.setCheckOut(checkOutTime);

        if (notes != null && !notes.trim().isEmpty()) {
            attendance.setNotes(attendance.getNotes() != null ? attendance.getNotes() + " | " + notes : notes);
        }

        double hours = (double) Duration.between(attendance.getCheckIn(), checkOutTime).toMinutes() / 60.0;
        hours = Math.round(hours * 100.0) / 100.0;
        attendance.setWorkHours(BigDecimal.valueOf(hours));

        if (hours < 4.0) {
            attendance.setStatus(AttendanceStatus.HALF_DAY);
        }

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Check-out successful for employee: {}, worked hours: {}", email, hours);
        return attendanceMapper.toResponse(saved);
    }

    @Transactional
    public AttendanceResponse addOrUpdateAttendance(AttendanceRequest request) {
        log.info("Admin/HR adjusting attendance log for employee ID: {}", request.getEmployeeId());
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(request.getEmployeeId(), request.getDate())
                .orElse(new Attendance());

        if (attendance.getId() == null) {
            attendance.setEmployee(employee);
            attendance.setDate(request.getDate());
        }

        attendance.setCheckIn(request.getCheckIn());
        attendance.setCheckOut(request.getCheckOut());
        attendance.setNotes(request.getNotes());

        if (request.getStatus() != null) {
            attendance.setStatus(request.getStatus());
        }

        if (request.getCheckIn() != null && request.getCheckOut() != null) {
            double hours = (double) Duration.between(request.getCheckIn(), request.getCheckOut()).toMinutes() / 60.0;
            hours = Math.round(hours * 100.0) / 100.0;
            attendance.setWorkHours(BigDecimal.valueOf(hours));
        }

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Attendance log updated: {}", saved.getId());
        return attendanceMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getEmployeeAttendanceHistory(UUID employeeId, LocalDate start, LocalDate end) {
        log.info("Fetching attendance history for employee: {} between {} and {}", employeeId, start, end);
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, start, end)
                .stream()
                .map(attendanceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getAttendanceList(LocalDate start, LocalDate end, UUID departmentId, Pageable pageable) {
        log.info("Admin fetching attendance list between {} and {}", start, end);
        Specification<Attendance> spec = Specification.where((root, query, cb) -> cb.equal(root.get("isDeleted"), false));

        if (start != null && end != null) {
            spec = spec.and((root, query, cb) -> cb.between(root.get("date"), start, end));
        }

        if (departmentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("employee").get("department").get("id"), departmentId));
        }

        return attendanceRepository.findAll(spec, pageable).map(attendanceMapper::toResponse);
    }
}
