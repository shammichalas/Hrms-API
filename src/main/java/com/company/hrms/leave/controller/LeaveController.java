package com.company.hrms.leave.controller;

import com.company.hrms.common.dto.ApiResponse;
import com.company.hrms.leave.dto.LeaveRequestDto;
import com.company.hrms.leave.dto.LeaveResponseDto;
import com.company.hrms.leave.entity.LeaveStatus;
import com.company.hrms.leave.service.LeaveService;
import com.company.hrms.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
@Tag(name = "Leave Management", description = "Endpoints for applying, listing, approving, and rejecting leave requests")
@SecurityRequirement(name = "bearerAuth")
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping("/apply")
    @Operation(summary = "Submit a new leave application for the current user")
    public ResponseEntity<ApiResponse<LeaveResponseDto>> applyLeave(@Valid @RequestBody LeaveRequestDto request) {
        String email = SecurityUtils.getCurrentUserEmail();
        LeaveResponseDto response = leaveService.applyLeave(email, request);
        return ResponseEntity.ok(ApiResponse.success("Leave application submitted successfully", response));
    }

    @PostMapping("/approve/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Approve a pending leave request (ADMIN/HR only)")
    public ResponseEntity<ApiResponse<LeaveResponseDto>> approveLeave(@PathVariable UUID id) {
        String email = SecurityUtils.getCurrentUserEmail();
        LeaveResponseDto response = leaveService.approveLeave(id, email);
        return ResponseEntity.ok(ApiResponse.success("Leave request approved successfully", response));
    }

    @PostMapping("/reject/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Reject a pending leave request (ADMIN/HR only)")
    public ResponseEntity<ApiResponse<LeaveResponseDto>> rejectLeave(
            @PathVariable UUID id,
            @RequestParam String reason
    ) {
        String email = SecurityUtils.getCurrentUserEmail();
        LeaveResponseDto response = leaveService.rejectLeave(id, reason, email);
        return ResponseEntity.ok(ApiResponse.success("Leave request rejected successfully", response));
    }

    @GetMapping("/my")
    @Operation(summary = "Retrieve a list of the current user's leave requests")
    public ResponseEntity<ApiResponse<List<LeaveResponseDto>>> getMyLeaves() {
        String email = SecurityUtils.getCurrentUserEmail();
        List<LeaveResponseDto> response = leaveService.getMyLeaves(email);
        return ResponseEntity.ok(ApiResponse.success("My leave applications retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Get a paginated and filtered list of all leave requests (ADMIN/HR only)")
    public ResponseEntity<ApiResponse<Page<LeaveResponseDto>>> getLeaves(
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) UUID departmentId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<LeaveResponseDto> response = leaveService.getLeaves(status, departmentId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Leave requests list retrieved successfully", response));
    }
}
