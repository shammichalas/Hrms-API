package com.company.hrms.leave.repository;

import com.company.hrms.leave.entity.LeaveRequest;
import com.company.hrms.leave.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRepository extends JpaRepository<LeaveRequest, UUID>, JpaSpecificationExecutor<LeaveRequest> {
    List<LeaveRequest> findByEmployeeId(UUID employeeId);
    List<LeaveRequest> findByStatus(LeaveStatus status);

    @Query("SELECT COUNT(l) > 0 FROM LeaveRequest l WHERE l.employee.id = :employeeId " +
           "AND l.status != 'REJECTED' AND l.isDeleted = false " +
           "AND ((l.startDate <= :endDate AND l.endDate >= :startDate))")
    boolean hasOverlappingLeaves(
            @Param("employeeId") UUID employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
