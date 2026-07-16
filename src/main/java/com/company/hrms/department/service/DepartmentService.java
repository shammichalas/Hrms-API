package com.company.hrms.department.service;

import com.company.hrms.common.exception.DuplicateResourceException;
import com.company.hrms.common.exception.ResourceNotFoundException;
import com.company.hrms.department.dto.CreateDepartmentRequest;
import com.company.hrms.department.dto.DepartmentResponse;
import com.company.hrms.department.entity.Department;
import com.company.hrms.department.mapper.DepartmentMapper;
import com.company.hrms.department.repository.DepartmentRepository;
import com.company.hrms.employee.entity.Employee;
import com.company.hrms.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentMapper departmentMapper;

    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        log.info("Creating department with code: {}", request.getCode());
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Department with code already exists: " + request.getCode());
        }

        Department department = departmentMapper.toEntity(request);

        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + request.getManagerId()));
            department.setManager(manager);
        }

        Department saved = departmentRepository.save(department);
        log.info("Department created successfully: {}", saved.getId());
        return departmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
        return departmentMapper.toResponse(department);
    }

    @Transactional
    public DepartmentResponse updateDepartment(UUID id, CreateDepartmentRequest request) {
        log.info("Updating department with ID: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        departmentMapper.updateEntityFromRequest(request, department);

        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + request.getManagerId()));
            department.setManager(manager);
        } else {
            department.setManager(null);
        }

        Department updated = departmentRepository.save(department);
        log.info("Department updated successfully: {}", updated.getId());
        return departmentMapper.toResponse(updated);
    }

    @Transactional
    public void deleteDepartment(UUID id) {
        log.info("Soft deleting department with ID: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
        department.setDeleted(true);
        departmentRepository.save(department);
        log.info("Department soft deleted: {}", id);
    }

    @Transactional(readOnly = true)
    public Page<DepartmentResponse> getDepartments(String search, Pageable pageable) {
        Specification<Department> spec = Specification.where((root, query, cb) -> cb.equal(root.get("isDeleted"), false));

        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), searchPattern),
                    cb.like(cb.lower(root.get("code")), searchPattern),
                    cb.like(cb.lower(root.get("description")), searchPattern)
            ));
        }

        return departmentRepository.findAll(spec, pageable).map(departmentMapper::toResponse);
    }
}
