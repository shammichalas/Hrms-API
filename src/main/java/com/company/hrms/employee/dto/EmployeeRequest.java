package com.company.hrms.employee.dto;

import com.company.hrms.employee.entity.EmployeeStatus;
import com.company.hrms.employee.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String phone;

    private UUID departmentId;

    private UUID managerId;

    private String jobTitle;

    @NotNull(message = "Status is required")
    private EmployeeStatus status;

    @PositiveOrZero(message = "Base salary must be greater than or equal to zero")
    private BigDecimal baseSalary = BigDecimal.ZERO;
}
