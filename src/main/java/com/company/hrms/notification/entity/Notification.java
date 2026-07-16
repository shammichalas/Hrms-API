package com.company.hrms.notification.entity;

import com.company.hrms.common.entity.BaseEntity;
import com.company.hrms.employee.entity.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@SQLDelete(sql = "UPDATE notifications SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Employee recipient;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(nullable = false, length = 20)
    private String type = "SYSTEM"; // e.g. SYSTEM, ATTENDANCE, LEAVE, PAYROLL
}
