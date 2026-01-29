package com.finance.planner.accounting.dto;

import com.finance.planner.accounting.entity.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionDto Unit Tests")
class TransactionDtoTest {

    // ========== Helper methods ==========

    private Transaction createTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setUserId(100L);
        transaction.setAmount(new BigDecimal("250.50"));
        transaction.setType((short) 2);
        transaction.setCategoryId(10L);
        transaction.setDescription("Grocery shopping");
        transaction.setTransactionDate(LocalDate.of(2025, 3, 15));
        transaction.setPaymentMethod("credit_card");
        transaction.setMerchant("Walmart");
        transaction.setIsDeleted(false);
        transaction.setCreatedAt(LocalDateTime.of(2025, 3, 15, 10, 30, 0));
        transaction.setUpdatedAt(LocalDateTime.of(2025, 3, 15, 10, 30, 0));
        return transaction;
    }

    // ========== fromEntity ==========

    @Test
    @DisplayName("fromEntity - maps all fields correctly without category details")
    void fromEntity_mapsAllFields() {
        // Given
        Transaction transaction = createTransaction();

        // When
        TransactionDto dto = TransactionDto.fromEntity(transaction);

        // Then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getAmount()).isEqualByComparingTo(new BigDecimal("250.50"));
        assertThat(dto.getType()).isEqualTo((short) 2);
        assertThat(dto.getCategoryId()).isEqualTo(10L);
        assertThat(dto.getDescription()).isEqualTo("Grocery shopping");
        assertThat(dto.getTransactionDate()).isEqualTo(LocalDate.of(2025, 3, 15));
        assertThat(dto.getPaymentMethod()).isEqualTo("credit_card");
        assertThat(dto.getMerchant()).isEqualTo("Walmart");
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 3, 15, 10, 30, 0));
        assertThat(dto.getUpdatedAt()).isEqualTo(LocalDateTime.of(2025, 3, 15, 10, 30, 0));
        // Category fields should be null
        assertThat(dto.getCategoryName()).isNull();
        assertThat(dto.getCategoryIcon()).isNull();
        assertThat(dto.getCategoryColor()).isNull();
    }

    // ========== fromEntityWithCategory ==========

    @Test
    @DisplayName("fromEntityWithCategory - adds category info to all entity fields")
    void fromEntityWithCategory_addsCategoryInfo() {
        // Given
        Transaction transaction = createTransaction();

        // When
        TransactionDto dto = TransactionDto.fromEntityWithCategory(
                transaction, "Food", "icon-food", "#FF5733");

        // Then
        // Verify all entity fields are mapped
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getAmount()).isEqualByComparingTo(new BigDecimal("250.50"));
        assertThat(dto.getType()).isEqualTo((short) 2);
        assertThat(dto.getCategoryId()).isEqualTo(10L);
        assertThat(dto.getDescription()).isEqualTo("Grocery shopping");
        assertThat(dto.getTransactionDate()).isEqualTo(LocalDate.of(2025, 3, 15));
        assertThat(dto.getPaymentMethod()).isEqualTo("credit_card");
        assertThat(dto.getMerchant()).isEqualTo("Walmart");

        // Verify category fields are set
        assertThat(dto.getCategoryName()).isEqualTo("Food");
        assertThat(dto.getCategoryIcon()).isEqualTo("icon-food");
        assertThat(dto.getCategoryColor()).isEqualTo("#FF5733");
    }
}
