package com.company.hrms.profile.service;

import com.company.hrms.common.exception.BadRequestException;
import com.company.hrms.common.exception.ResourceNotFoundException;
import com.company.hrms.employee.dto.EmployeeResponse;
import com.company.hrms.employee.entity.Employee;
import com.company.hrms.employee.mapper.EmployeeMapper;
import com.company.hrms.employee.repository.EmployeeRepository;
import com.company.hrms.profile.dto.PasswordChangeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public EmployeeResponse getProfile(String email) {
        log.info("Fetching profile details for: {}", email);
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with email: " + email));
        return employeeMapper.toResponse(employee);
    }

    @Transactional
    public void changePassword(String email, PasswordChangeRequest request) {
        log.info("Password change attempt for: {}", email);
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with email: " + email));

        if (!passwordEncoder.matches(request.getOldPassword(), employee.getPasswordHash())) {
            throw new BadRequestException("Current password does not match");
        }

        employee.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        employeeRepository.save(employee);
        log.info("Password changed successfully for user: {}", email);
    }
}
