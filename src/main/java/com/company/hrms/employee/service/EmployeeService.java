package com.company.hrms.employee.service;

import com.company.hrms.common.exception.DuplicateResourceException;
import com.company.hrms.common.exception.ResourceNotFoundException;
import com.company.hrms.department.entity.Department;
import com.company.hrms.department.repository.DepartmentRepository;
import com.company.hrms.employee.dto.EmployeeRequest;
import com.company.hrms.employee.dto.EmployeeResponse;
import com.company.hrms.employee.dto.UpdateEmployeeRequest;
import com.company.hrms.employee.entity.Employee;
import com.company.hrms.employee.entity.EmployeeStatus;
import com.company.hrms.employee.entity.Role;
import com.company.hrms.employee.mapper.EmployeeMapper;
import com.company.hrms.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        log.info("Creating employee with email: {}", request.getEmail());
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Employee with email already exists: " + request.getEmail());
        }

        Employee employee = employeeMapper.toEntity(request);
        employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));
            employee.setDepartment(department);
        }

        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + request.getManagerId()));
            employee.setManager(manager);
        }

        Employee saved = employeeRepository.save(employee);
        log.info("Employee created successfully: {}", saved.getId());
        return employeeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
        return employeeMapper.toResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(UUID id, UpdateEmployeeRequest request) {
        log.info("Updating employee with ID: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        employeeMapper.updateEntityFromRequest(request, employee);

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));
            employee.setDepartment(department);
        } else {
            employee.setDepartment(null);
        }

        if (request.getManagerId() != null) {
            if (id.equals(request.getManagerId())) {
                throw new IllegalArgumentException("An employee cannot be their own manager");
            }
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + request.getManagerId()));
            employee.setManager(manager);
        } else {
            employee.setManager(null);
        }

        Employee updated = employeeRepository.save(employee);
        log.info("Employee updated successfully: {}", updated.getId());
        return employeeMapper.toResponse(updated);
    }

    @Transactional
    public void deleteEmployee(UUID id) {
        log.info("Soft deleting employee with ID: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
        employee.setDeleted(true);
        employeeRepository.save(employee);
        log.info("Employee soft deleted: {}", id);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getEmployees(String search, UUID departmentId, Role role, EmployeeStatus status, Pageable pageable) {
        Specification<Employee> spec = Specification.where((root, query, cb) -> cb.equal(root.get("isDeleted"), false));

        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("firstName")), searchPattern),
                    cb.like(cb.lower(root.get("lastName")), searchPattern),
                    cb.like(cb.lower(root.get("email")), searchPattern),
                    cb.like(cb.lower(root.get("jobTitle")), searchPattern)
            ));
        }

        if (departmentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("department").get("id"), departmentId));
        }

        if (role != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), role));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return employeeRepository.findAll(spec, pageable).map(employeeMapper::toResponse);
    }
}
