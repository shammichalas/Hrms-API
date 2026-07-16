package com.company.hrms.payroll.service;

import com.company.hrms.common.exception.BadRequestException;
import com.company.hrms.common.exception.ResourceNotFoundException;
import com.company.hrms.employee.entity.Employee;
import com.company.hrms.employee.repository.EmployeeRepository;
import com.company.hrms.payroll.dto.PayrollRequest;
import com.company.hrms.payroll.dto.PayrollResponse;
import com.company.hrms.payroll.entity.PaymentStatus;
import com.company.hrms.payroll.entity.Payroll;
import com.company.hrms.payroll.mapper.PayrollMapper;
import com.company.hrms.payroll.repository.PayrollRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollMapper payrollMapper;

    @Transactional
    public PayrollResponse createPayroll(PayrollRequest request) {
        log.info("Creating payroll record for employee ID: {}", request.getEmployeeId());
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        if (request.getPayPeriodStart().isAfter(request.getPayPeriodEnd())) {
            throw new BadRequestException("Pay period start date cannot be after end date");
        }

        Payroll payroll = payrollMapper.toEntity(request);
        payroll.setEmployee(employee);

        BigDecimal basicSalary = employee.getBaseSalary();
        payroll.setBasicSalary(basicSalary);

        BigDecimal netSalary = basicSalary.add(request.getAllowances()).subtract(request.getDeductions());
        if (netSalary.compareTo(BigDecimal.ZERO) < 0) {
            netSalary = BigDecimal.ZERO;
        }
        payroll.setNetSalary(netSalary);

        if (request.getPaymentStatus() == PaymentStatus.PAID) {
            payroll.setPaymentDate(LocalDateTime.now());
        }

        payroll.setPaySlipUrl("/api/payroll/slips/" + UUID.randomUUID() + ".pdf");

        Payroll saved = payrollRepository.save(payroll);
        log.info("Payroll record created: {}", saved.getId());
        return payrollMapper.toResponse(saved);
    }

    @Transactional
    public PayrollResponse updatePayroll(UUID id, PayrollRequest request) {
        log.info("Updating payroll record with ID: {}", id);
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with ID: " + id));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        payrollMapper.updateEntityFromRequest(request, payroll);
        payroll.setEmployee(employee);

        BigDecimal basicSalary = employee.getBaseSalary();
        payroll.setBasicSalary(basicSalary);

        BigDecimal netSalary = basicSalary.add(request.getAllowances()).subtract(request.getDeductions());
        if (netSalary.compareTo(BigDecimal.ZERO) < 0) {
            netSalary = BigDecimal.ZERO;
        }
        payroll.setNetSalary(netSalary);

        if (request.getPaymentStatus() == PaymentStatus.PAID && payroll.getPaymentDate() == null) {
            payroll.setPaymentDate(LocalDateTime.now());
        }

        Payroll updated = payrollRepository.save(payroll);
        log.info("Payroll record updated: {}", updated.getId());
        return payrollMapper.toResponse(updated);
    }

    @Transactional
    public PayrollResponse payPayroll(UUID id) {
        log.info("Processing payment for payroll: {}", id);
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with ID: " + id));

        if (payroll.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Payroll is already paid.");
        }

        payroll.setPaymentStatus(PaymentStatus.PAID);
        payroll.setPaymentDate(LocalDateTime.now());

        Payroll saved = payrollRepository.save(payroll);
        log.info("Payment registered successfully");
        return payrollMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PayrollResponse> getMyPayrolls(String employeeEmail) {
        log.info("Fetching payrolls history for current user: {}", employeeEmail);
        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + employeeEmail));

        return payrollRepository.findByEmployeeId(employee.getId())
                .stream()
                .map(payrollMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PayrollResponse> getPayrolls(UUID departmentId, PaymentStatus status, Pageable pageable) {
        log.info("Admin fetching payroll logs list");
        Specification<Payroll> spec = Specification.where((root, query, cb) -> cb.equal(root.get("isDeleted"), false));

        if (departmentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("employee").get("department").get("id"), departmentId));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("paymentStatus"), status));
        }

        return payrollRepository.findAll(spec, pageable).map(payrollMapper::toResponse);
    }
}
