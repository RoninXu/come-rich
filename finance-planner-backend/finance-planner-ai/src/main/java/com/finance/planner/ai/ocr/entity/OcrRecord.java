package com.finance.planner.ai.ocr.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ocr_record", indexes = {
        @Index(name = "idx_ocr_record_user_id", columnList = "user_id"),
        @Index(name = "idx_ocr_record_status", columnList = "status"),
        @Index(name = "idx_ocr_record_user_status", columnList = "user_id, status")
})
public class OcrRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "ocr_raw_text", columnDefinition = "TEXT")
    private String ocrRawText;

    @Column(name = "extracted_amount", precision = 12, scale = 2)
    private BigDecimal extractedAmount;

    @Column(name = "extracted_merchant", length = 200)
    private String extractedMerchant;

    @Column(name = "extracted_date")
    private LocalDate extractedDate;

    @Column(name = "suggested_category_id")
    private Long suggestedCategoryId;

    @Column(name = "transaction_id")
    private Long transactionId;

    /** 1=pending, 2=confirmed, 3=rejected */
    @Column(nullable = false)
    private Short status = 1;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
