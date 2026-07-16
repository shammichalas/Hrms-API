package com.company.hrms.leave.service;

import com.company.hrms.common.exception.BadRequestException;
import com.company.hrms.common.exception.ResourceNotFoundException;
import com.company.hrms.employee.entity.Employee;
import com.company.hrms.employee.repository.EmployeeRepository;
import com.company.hrms.leave.dto.LeaveRequestDto;
import com.company.hrms.leave.dto.LeaveResponseDto;
import com.company.hrms.leave.entity.LeaveRequest;
import com.company.hrms.leave.entity.LeaveStatus;
import com.company.hrms.leave.mapper.LeaveMapper;
import com.company.hrms.leave.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveMapper leaveMapper;

    @Transactional
    public LeaveResponseDto applyLeave(String employeeEmail, LeaveRequestDto request) {
        log.info("Employee applying for leave: {}", employeeEmail);
        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + employeeEmail));

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        boolean overlaps = leaveRepository.hasOverlappingLeaves(
                employee.getId(),
                request.getStartDate(),
                request.getEndDate()
        );
        if (overlaps) {
            throw new BadRequestException("You have another leave request overlapping with these dates");
        }

        LeaveRequest leaveRequest = leaveMapper.toEntity(request);
        leaveRequest.setEmployee(employee);
        leaveRequest.setStatus(LeaveStatus.PENDING);

        LeaveRequest saved = leaveRepository.save(leaveRequest);
        log.info("Leave request created successfully: {}", saved.getId());
        return leaveMapper.toResponse(saved);
    }

    @Transactional
    public LeaveResponseDto approveLeave(UUID leaveRequestId, String approverEmail) {
        log.info("Approving leave request: {} by {}", leaveRequestId, approverEmail);
        Employee approver = employeeRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found with email: " + approverEmail));

        LeaveRequest leaveRequest = leaveRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + leaveRequestId));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Leave request is already processed. Current status: " + leaveRequest.getStatus());
        }

        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setApprovedDate(LocalDateTime.now());

        LeaveRequest saved = leaveRepository.save(leaveRequest);
        log.info("Leave request approved successfully");
        return leaveMapper.toResponse(saved);
    }

    @Transactional
    public LeaveResponseDto rejectLeave(UUID leaveRequestId, String rejectionReason, String approverEmail) {
        log.info("Rejecting leave request: {} by {}", leaveRequestId, approverEmail);
        Employee approver = employeeRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found with email: " + approverEmail));

        LeaveRequest leaveRequest = leaveRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + leaveRequestId));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Leave request is already processed. Current status: " + leaveRequest.getStatus());
        }

        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setApprovedDate(LocalDateTime.now());
        leaveRequest.setRejectionReason(rejectionReason);

        LeaveRequest saved = leaveRepository.save(leaveRequest);
        log.info("Leave request rejected successfully");
        return leaveMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponseDto> getMyLeaves(String employeeEmail) {
        log.info("Fetching leaves history for current user: {}", employeeEmail);
        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + employeeEmail));

        return leaveRepository.findByEmployeeId(employee.getId())
                .stream()
                .map(leaveMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<LeaveResponseDto> getLeaves(LeaveStatus status, UUID departmentId, Pageable pageable) {
        log.info("Admin fetching leaves list");
        Specification<LeaveRequest> spec = Specification.where((root, query, cb) -> cb.equal(root.get("isDeleted"), false));

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (departmentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("employee").get("department").get("id"), departmentId));
        }

        return leaveRepository.findAll(spec, pageable).map(leaveMapper::toResponse);
    }
}
