package com.company.hrms.payroll.mapper;

import com.company.hrms.payroll.dto.PayrollRequest;
import com.company.hrms.payroll.dto.PayrollResponse;
import com.company.hrms.payroll.entity.Payroll;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PayrollMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", expression = "java(payroll.getEmployee().getFirstName() + \" \" + payroll.getEmployee().getLastName())")
    PayrollResponse toResponse(Payroll payroll);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "basicSalary", ignore = true)
    @Mapping(target = "netSalary", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "paySlipUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Payroll toEntity(PayrollRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "basicSalary", ignore = true)
    @Mapping(target = "netSalary", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "paySlipUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromRequest(PayrollRequest request, @MappingTarget Payroll payroll);
}
