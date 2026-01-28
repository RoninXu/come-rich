package com.finance.planner.accounting.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "category", indexes = {
        @Index(name = "idx_category_type", columnList = "type"),
        @Index(name = "idx_category_parent", columnList = "parent_id")
})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false)
    private Short type;

    @Column(length = 50)
    private String icon;

    @Column(length = 20)
    private String color;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_system")
    private Boolean isSystem = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Check if this is an income category
     */
    public boolean isIncome() {
        return type != null && type == 1;
    }

    /**
     * Check if this is an expense category
     */
    public boolean isExpense() {
        return type != null && type == 2;
    }

    /**
     * Check if this is a parent category (no parent_id)
     */
    public boolean isParent() {
        return parentId == null;
    }
}
