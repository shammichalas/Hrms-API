package com.company.hrms.settings.controller;

import com.company.hrms.common.dto.ApiResponse;
import com.company.hrms.settings.entity.Settings;
import com.company.hrms.settings.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Tag(name = "Settings Management", description = "Endpoints for viewing and updating system configurations")
@SecurityRequirement(name = "bearerAuth")
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    @Operation(summary = "Get a list of all company system settings")
    public ResponseEntity<ApiResponse<List<Settings>>> getAllSettings() {
        List<Settings> response = settingsService.getAllSettings();
        return ResponseEntity.ok(ApiResponse.success("System configurations retrieved successfully", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update or add a company setting (ADMIN only)")
    public ResponseEntity<ApiResponse<Settings>> updateSetting(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam(required = false) String description
    ) {
        Settings response = settingsService.updateSetting(key, value, description);
        return ResponseEntity.ok(ApiResponse.success("Setting updated successfully", response));
    }
}
