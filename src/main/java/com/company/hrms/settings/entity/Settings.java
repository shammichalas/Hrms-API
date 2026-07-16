package com.company.hrms.settings.entity;

import com.company.hrms.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "settings")
@Getter
@Setter
@SQLDelete(sql = "UPDATE settings SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Settings extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String key;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String value;

    @Column(columnDefinition = "TEXT")
    private String description;
}
