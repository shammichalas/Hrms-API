package com.company.hrms.analytics.controller;

import com.company.hrms.analytics.dto.AnalyticsStats;
import com.company.hrms.analytics.service.AnalyticsService;
import com.company.hrms.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Endpoints for retrieving organizational statistical summaries")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Get corporate statistics and average working hours (ADMIN/HR only)")
    public ResponseEntity<ApiResponse<AnalyticsStats>> getAnalytics() {
        AnalyticsStats stats = analyticsService.getAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Analytics metrics retrieved successfully", stats));
    }
}
