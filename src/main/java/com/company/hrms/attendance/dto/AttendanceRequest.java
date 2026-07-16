package com.company.hrms.attendance.dto;

import com.company.hrms.attendance.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequest {

    private UUID employeeId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Check-in time is required")
    private LocalDateTime checkIn;

    private LocalDateTime checkOut;

    private AttendanceStatus status;

    private String notes;
}
