package com.company.hrms.payroll.dto;

import com.company.hrms.payroll.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollRequest {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotNull(message = "Pay period start date is required")
    private LocalDate payPeriodStart;

    @NotNull(message = "Pay period end date is required")
    private LocalDate payPeriodEnd;

    @PositiveOrZero(message = "Allowances must be positive or zero")
    private BigDecimal allowances = BigDecimal.ZERO;

    @PositiveOrZero(message = "Deductions must be positive or zero")
    private BigDecimal deductions = BigDecimal.ZERO;

    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;
}
