package com.company.hrms.notification.service;

import com.company.hrms.common.exception.BadRequestException;
import com.company.hrms.common.exception.ResourceNotFoundException;
import com.company.hrms.employee.entity.Employee;
import com.company.hrms.employee.repository.EmployeeRepository;
import com.company.hrms.notification.dto.NotificationResponse;
import com.company.hrms.notification.entity.Notification;
import com.company.hrms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public void sendNotification(UUID recipientId, String title, String message, String type) {
        log.info("Sending notification of type {} to employee: {}", type, recipientId);
        Employee employee = employeeRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found with ID: " + recipientId));

        Notification notification = new Notification();
        notification.setRecipient(employee);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);

        notificationRepository.save(notification);
        log.info("Notification sent successfully");
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(String email, Pageable pageable) {
        log.info("Fetching notifications for employee: {}", email);
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));

        return notificationRepository.findByRecipientId(employee.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public void markAsRead(UUID id, String email) {
        log.info("Marking notification {} as read for {}", id, email);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));

        if (!notification.getRecipient().getEmail().equals(email)) {
            throw new BadRequestException("Unauthorized access to this notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String email) {
        log.info("Marking all notifications as read for {}", email);
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));

        List<Notification> unread = notificationRepository.findByRecipientIdAndIsReadFalse(employee.getId());
        unread.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unread);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipient().getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .type(notification.getType())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
