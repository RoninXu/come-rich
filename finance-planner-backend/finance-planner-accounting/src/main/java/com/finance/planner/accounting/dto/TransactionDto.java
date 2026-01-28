package com.finance.planner.accounting.dto;

import com.finance.planner.accounting.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {

    private Long id;
    private BigDecimal amount;
    private Short type;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private String description;
    private LocalDate transactionDate;
    private String paymentMethod;
    private String merchant;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert entity to DTO (without category details)
     */
    public static TransactionDto fromEntity(Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .categoryId(transaction.getCategoryId())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .paymentMethod(transaction.getPaymentMethod())
                .merchant(transaction.getMerchant())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }

    /**
     * Convert entity to DTO with category details
     */
    public static TransactionDto fromEntityWithCategory(Transaction transaction,
                                                         String categoryName,
                                                         String categoryIcon,
                                                         String categoryColor) {
        TransactionDto dto = fromEntity(transaction);
        dto.setCategoryName(categoryName);
        dto.setCategoryIcon(categoryIcon);
        dto.setCategoryColor(categoryColor);
        return dto;
    }
}
