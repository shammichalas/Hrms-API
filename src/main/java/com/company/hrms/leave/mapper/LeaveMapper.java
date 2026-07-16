package com.company.hrms.leave.mapper;

import com.company.hrms.leave.dto.LeaveRequestDto;
import com.company.hrms.leave.dto.LeaveResponseDto;
import com.company.hrms.leave.entity.LeaveRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeaveMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", expression = "java(leaveRequest.getEmployee().getFirstName() + \" \" + leaveRequest.getEmployee().getLastName())")
    @Mapping(target = "approvedById", source = "approvedBy.id")
    @Mapping(target = "approvedByName", expression = "java(leaveRequest.getApprovedBy() != null ? (leaveRequest.getApprovedBy().getFirstName() + \" \" + leaveRequest.getApprovedBy().getLastName()) : null)")
    LeaveResponseDto toResponse(LeaveRequest leaveRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedDate", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    LeaveRequest toEntity(LeaveRequestDto request);
}
