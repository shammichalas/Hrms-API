package com.company.hrms.payroll.controller;

import com.company.hrms.common.dto.ApiResponse;
import com.company.hrms.payroll.dto.PayrollRequest;
import com.company.hrms.payroll.dto.PayrollResponse;
import com.company.hrms.payroll.entity.PaymentStatus;
import com.company.hrms.payroll.service.PayrollService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
@Tag(name = "Payroll Management", description = "Endpoints for generating, updating, paying, and listing employee payslips")
@SecurityRequirement(name = "bearerAuth")
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Generate a new payroll record for an employee (ADMIN/HR only)")
    public ResponseEntity<ApiResponse<PayrollResponse>> createPayroll(@Valid @RequestBody PayrollRequest request) {
        PayrollResponse response = payrollService.createPayroll(request);
        return ResponseEntity.ok(ApiResponse.success("Payroll record created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update an existing payroll entry (ADMIN/HR only)")
    public ResponseEntity<ApiResponse<PayrollResponse>> updatePayroll(
            @PathVariable UUID id,
            @Valid @RequestBody PayrollRequest request
    ) {
        PayrollResponse response = payrollService.updatePayroll(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payroll record updated successfully", response));
    }

    @PostMapping("/pay/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Mark a payroll record as PAID (ADMIN/HR only)")
    public ResponseEntity<ApiResponse<PayrollResponse>> payPayroll(@PathVariable UUID id) {
        PayrollResponse response = payrollService.payPayroll(id);
        return ResponseEntity.ok(ApiResponse.success("Payroll processed and marked as paid successfully", response));
    }

    @GetMapping("/my")
    @Operation(summary = "Retrieve a list of the current user's payroll payslips")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getMyPayrolls() {
        String email = SecurityUtils.getCurrentUserEmail();
        List<PayrollResponse> response = payrollService.getMyPayrolls(email);
        return ResponseEntity.ok(ApiResponse.success("My payroll records retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Get a paginated and filtered list of all employee payroll entries (ADMIN/HR only)")
    public ResponseEntity<ApiResponse<Page<PayrollResponse>>> getPayrolls(
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<PayrollResponse> response = payrollService.getPayrolls(departmentId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Payroll records list retrieved successfully", response));
    }
}
