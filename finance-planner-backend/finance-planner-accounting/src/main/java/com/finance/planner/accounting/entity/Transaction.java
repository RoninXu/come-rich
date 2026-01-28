package com.finance.planner.accounting.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transaction", indexes = {
        @Index(name = "idx_transaction_user_id", columnList = "user_id"),
        @Index(name = "idx_transaction_date", columnList = "transaction_date"),
        @Index(name = "idx_transaction_category", columnList = "category_id"),
        @Index(name = "idx_transaction_type", columnList = "type"),
        @Index(name = "idx_transaction_user_date", columnList = "user_id, transaction_date"),
        @Index(name = "idx_transaction_user_type_date", columnList = "user_id, type, transaction_date")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private Short type;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(length = 255)
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(length = 100)
    private String merchant;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Check if this is an income transaction
     */
    public boolean isIncome() {
        return type != null && type == 1;
    }

    /**
     * Check if this is an expense transaction
     */
    public boolean isExpense() {
        return type != null && type == 2;
    }
}
