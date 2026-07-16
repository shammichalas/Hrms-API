package com.company.hrms.profile.controller;

import com.company.hrms.common.dto.ApiResponse;
import com.company.hrms.employee.dto.EmployeeResponse;
import com.company.hrms.profile.dto.PasswordChangeRequest;
import com.company.hrms.profile.service.ProfileService;
import com.company.hrms.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Endpoints for employee self-service profile and password changes")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Operation(summary = "Get the current authenticated user's profile details")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getProfile() {
        String email = SecurityUtils.getCurrentUserEmail();
        EmployeeResponse response = profileService.getProfile(email);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", response));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for the current authenticated user")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        String email = SecurityUtils.getCurrentUserEmail();
        profileService.changePassword(email, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }
}
