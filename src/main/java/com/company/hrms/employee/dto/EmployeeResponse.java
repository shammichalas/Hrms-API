package com.company.hrms.employee.dto;

import com.company.hrms.employee.entity.EmployeeStatus;
import com.company.hrms.employee.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {
    private UUID id;
    private String email;
    private Role role;
    private String firstName;
    private String lastName;
    private String phone;
    private UUID departmentId;
    private String departmentName;
    private UUID managerId;
    private String managerName;
    private String jobTitle;
    private EmployeeStatus status;
    private BigDecimal baseSalary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
