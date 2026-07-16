package com.company.hrms.notification.controller;

import com.company.hrms.common.dto.ApiResponse;
import com.company.hrms.notification.dto.NotificationResponse;
import com.company.hrms.notification.service.NotificationService;
import com.company.hrms.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for employee alerts and read tracking")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get a paginated list of the current user's notifications")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        String email = SecurityUtils.getCurrentUserEmail();
        Page<NotificationResponse> response = notificationService.getMyNotifications(email, pageable);
        return ResponseEntity.ok(ApiResponse.success("Notifications list retrieved successfully", response));
    }

    @PostMapping("/read/{id}")
    @Operation(summary = "Mark a specific notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID id) {
        String email = SecurityUtils.getCurrentUserEmail();
        notificationService.markAsRead(id, email);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read successfully"));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all of the current user's notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        String email = SecurityUtils.getCurrentUserEmail();
        notificationService.markAllAsRead(email);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read successfully"));
    }
}
