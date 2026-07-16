package com.company.hrms.payroll.dto;

import com.company.hrms.payroll.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollResponse {
    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;
    private BigDecimal basicSalary;
    private BigDecimal allowances;
    private BigDecimal deductions;
    private BigDecimal netSalary;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
    private String paySlipUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
