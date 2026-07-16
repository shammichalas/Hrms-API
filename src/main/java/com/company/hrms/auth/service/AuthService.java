package com.company.hrms.auth.service;

import com.company.hrms.auth.dto.AuthResponse;
import com.company.hrms.auth.dto.LoginRequest;
import com.company.hrms.auth.dto.RegisterRequest;
import com.company.hrms.auth.dto.TokenRefreshRequest;
import com.company.hrms.common.exception.BadRequestException;
import com.company.hrms.common.exception.DuplicateResourceException;
import com.company.hrms.common.exception.UnauthorizedException;
import com.company.hrms.employee.entity.Employee;
import com.company.hrms.employee.entity.EmployeeStatus;
import com.company.hrms.employee.entity.Role;
import com.company.hrms.employee.repository.EmployeeRepository;
import com.company.hrms.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already taken: " + request.getEmail());
        }

        Employee employee = new Employee();
        employee.setEmail(request.getEmail());
        employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        employee.setRole(Role.EMPLOYEE); // By default new registrants are EMPLOYEE
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setPhone(request.getPhone());
        employee.setJobTitle(request.getJobTitle());
        employee.setStatus(EmployeeStatus.ACTIVE);

        Employee saved = employeeRepository.save(employee);
        log.info("User registered successfully: {}", saved.getEmail());

        UserDetails userDetails = userDetailsService.loadUserByUsername(saved.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(saved.getEmail())
                .role(saved.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getEmail());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid email or password");
        }

        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new BadRequestException("User account is " + employee.getStatus().name().toLowerCase());
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(employee.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("User authenticated successfully: {}", employee.getEmail());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(employee.getEmail())
                .role(employee.getRole().name())
                .build();
    }

    public AuthResponse refresh(TokenRefreshRequest request) {
        String token = request.getRefreshToken();
        try {
            String userEmail = jwtService.extractUsername(token);
            if (userEmail != null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                if (jwtService.isTokenValid(token, userDetails)) {
                    String accessToken = jwtService.generateToken(userDetails);
                    String newRefreshToken = jwtService.generateRefreshToken(userDetails);

                    return AuthResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(newRefreshToken)
                            .email(userDetails.getUsername())
                            .role(userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""))
                            .build();
                }
            }
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid refresh token: " + e.getMessage());
        }
        throw new UnauthorizedException("Invalid refresh token");
    }
}
