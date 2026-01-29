package com.finance.planner.accounting.service.impl;

import com.finance.planner.accounting.dto.CreateTransactionRequest;
import com.finance.planner.accounting.dto.TransactionDto;
import com.finance.planner.accounting.dto.UpdateTransactionRequest;
import com.finance.planner.accounting.entity.Category;
import com.finance.planner.accounting.entity.Transaction;
import com.finance.planner.accounting.repository.CategoryRepository;
import com.finance.planner.accounting.repository.TransactionRepository;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.common.response.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionServiceImpl Unit Tests")
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private static final Long USER_ID = 1L;
    private static final Long TRANSACTION_ID = 100L;
    private static final Long CATEGORY_ID = 10L;

    // ========== Helper methods ==========

    private Transaction createTransaction(Long id, Long userId, BigDecimal amount, Short type,
                                           Long categoryId, LocalDate date) {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setCategoryId(categoryId);
        transaction.setDescription("Test transaction");
        transaction.setTransactionDate(date);
        transaction.setPaymentMethod("cash");
        transaction.setMerchant("Test Merchant");
        transaction.setIsDeleted(false);
        return transaction;
    }

    private Category createCategory(Long id, String name, Short type) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setType(type);
        category.setIcon("icon-" + name.toLowerCase());
        category.setColor("#FF0000");
        category.setSortOrder(1);
        category.setIsSystem(true);
        return category;
    }

    private CreateTransactionRequest createTransactionRequest(BigDecimal amount, Short type, Long categoryId) {
        return CreateTransactionRequest.builder()
                .amount(amount)
                .type(type)
                .categoryId(categoryId)
                .description("Test transaction")
                .transactionDate(LocalDate.of(2025, 1, 15))
                .paymentMethod("cash")
                .merchant("Test Merchant")
                .build();
    }

    // ========== createTransaction ==========

    @Test
    @DisplayName("createTransaction - success with matching category type")
    void createTransaction_success() {
        // Given
        Short expenseType = 2;
        Category category = createCategory(CATEGORY_ID, "Food", expenseType);
        CreateTransactionRequest request = createTransactionRequest(
                new BigDecimal("50.00"), expenseType, CATEGORY_ID);

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(TRANSACTION_ID);
            return t;
        });

        // When
        TransactionDto result = transactionService.createTransaction(USER_ID, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getType()).isEqualTo(expenseType);
        assertThat(result.getCategoryName()).isEqualTo("Food");
        assertThat(result.getCategoryIcon()).isEqualTo("icon-food");
        assertThat(result.getCategoryColor()).isEqualTo("#FF0000");
        verify(categoryRepository).findById(CATEGORY_ID);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("createTransaction - throws when category not found")
    void createTransaction_categoryNotFound() {
        // Given
        CreateTransactionRequest request = createTransactionRequest(
                new BigDecimal("50.00"), (short) 2, CATEGORY_ID);
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> transactionService.createTransaction(USER_ID, request))
                .isInstanceOf(BusinessException.class);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTransaction - throws when category type does not match transaction type")
    void createTransaction_categoryTypeMismatch() {
        // Given
        Category incomeCategory = createCategory(CATEGORY_ID, "Salary", (short) 1);
        CreateTransactionRequest request = createTransactionRequest(
                new BigDecimal("50.00"), (short) 2, CATEGORY_ID); // expense type with income category

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(incomeCategory));

        // When / Then
        assertThatThrownBy(() -> transactionService.createTransaction(USER_ID, request))
                .isInstanceOf(BusinessException.class);
        verify(transactionRepository, never()).save(any());
    }

    // ========== updateTransaction ==========

    @Test
    @DisplayName("updateTransaction - success with full update")
    void updateTransaction_success() {
        // Given
        Transaction existing = createTransaction(TRANSACTION_ID, USER_ID,
                new BigDecimal("50.00"), (short) 2, CATEGORY_ID, LocalDate.of(2025, 1, 15));
        Category newCategory = createCategory(20L, "Transport", (short) 2);

        UpdateTransactionRequest request = UpdateTransactionRequest.builder()
                .amount(new BigDecimal("100.00"))
                .categoryId(20L)
                .description("Updated")
                .transactionDate(LocalDate.of(2025, 2, 1))
                .paymentMethod("card")
                .merchant("New Merchant")
                .build();

        when(transactionRepository.findByIdAndUserIdAndIsDeletedFalse(TRANSACTION_ID, USER_ID))
                .thenReturn(Optional.of(existing));
        when(categoryRepository.findById(20L)).thenReturn(Optional.of(newCategory));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        // For enrichWithCategory call
        when(categoryRepository.findById(20L)).thenReturn(Optional.of(newCategory));

        // When
        TransactionDto result = transactionService.updateTransaction(USER_ID, TRANSACTION_ID, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.getCategoryName()).isEqualTo("Transport");
        verify(transactionRepository).findByIdAndUserIdAndIsDeletedFalse(TRANSACTION_ID, USER_ID);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("updateTransaction - throws when transaction not found")
    void updateTransaction_notFound() {
        // Given
        UpdateTransactionRequest request = UpdateTransactionRequest.builder()
                .amount(new BigDecimal("100.00"))
                .build();
        when(transactionRepository.findByIdAndUserIdAndIsDeletedFalse(TRANSACTION_ID, USER_ID))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> transactionService.updateTransaction(USER_ID, TRANSACTION_ID, request))
                .isInstanceOf(BusinessException.class);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateTransaction - partial update only changes provided fields")
    void updateTransaction_partialUpdate() {
        // Given
        Transaction existing = createTransaction(TRANSACTION_ID, USER_ID,
                new BigDecimal("50.00"), (short) 2, CATEGORY_ID, LocalDate.of(2025, 1, 15));
        Category existingCategory = createCategory(CATEGORY_ID, "Food", (short) 2);

        UpdateTransactionRequest request = UpdateTransactionRequest.builder()
                .description("Only description changed")
                .build();

        when(transactionRepository.findByIdAndUserIdAndIsDeletedFalse(TRANSACTION_ID, USER_ID))
                .thenReturn(Optional.of(existing));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(existingCategory));

        // When
        TransactionDto result = transactionService.updateTransaction(USER_ID, TRANSACTION_ID, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("50.00")); // unchanged
        assertThat(result.getCategoryName()).isEqualTo("Food");
        verify(transactionRepository).save(any(Transaction.class));
        // Category findById should NOT be called for validation (no categoryId in request)
        // but will be called once for enrichWithCategory
        verify(categoryRepository, times(1)).findById(CATEGORY_ID);
    }

    @Test
    @DisplayName("updateTransaction - throws when changing category with type mismatch")
    void updateTransaction_changeCategoryWithTypeMismatch() {
        // Given
        Transaction existing = createTransaction(TRANSACTION_ID, USER_ID,
                new BigDecimal("50.00"), (short) 2, CATEGORY_ID, LocalDate.of(2025, 1, 15));
        Category incomeCategory = createCategory(20L, "Salary", (short) 1);

        UpdateTransactionRequest request = UpdateTransactionRequest.builder()
                .categoryId(20L) // income category for expense transaction
                .build();

        when(transactionRepository.findByIdAndUserIdAndIsDeletedFalse(TRANSACTION_ID, USER_ID))
                .thenReturn(Optional.of(existing));
        when(categoryRepository.findById(20L)).thenReturn(Optional.of(incomeCategory));

        // When / Then
        assertThatThrownBy(() -> transactionService.updateTransaction(USER_ID, TRANSACTION_ID, request))
                .isInstanceOf(BusinessException.class);
        verify(transactionRepository, never()).save(any());
    }

    // ========== deleteTransaction ==========

    @Test
    @DisplayName("deleteTransaction - soft deletes transaction successfully")
    void deleteTransaction_success() {
        // Given
        Transaction existing = createTransaction(TRANSACTION_ID, USER_ID,
                new BigDecimal("50.00"), (short) 2, CATEGORY_ID, LocalDate.of(2025, 1, 15));

        when(transactionRepository.findByIdAndUserIdAndIsDeletedFalse(TRANSACTION_ID, USER_ID))
                .thenReturn(Optional.of(existing));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        // When
        transactionService.deleteTransaction(USER_ID, TRANSACTION_ID);

        // Then
        assertThat(existing.getIsDeleted()).isTrue();
        verify(transactionRepository).save(existing);
    }

    @Test
    @DisplayName("deleteTransaction - throws when transaction not found")
    void deleteTransaction_notFound_throws() {
        // Given
        when(transactionRepository.findByIdAndUserIdAndIsDeletedFalse(TRANSACTION_ID, USER_ID))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> transactionService.deleteTransaction(USER_ID, TRANSACTION_ID))
                .isInstanceOf(BusinessException.class);
        verify(transactionRepository, never()).save(any());
    }

    // ========== getTransaction ==========

    @Test
    @DisplayName("getTransaction - returns transaction with category details when found")
    void getTransaction_success() {
        // Given
        Transaction existing = createTransaction(TRANSACTION_ID, USER_ID,
                new BigDecimal("50.00"), (short) 2, CATEGORY_ID, LocalDate.of(2025, 1, 15));
        Category category = createCategory(CATEGORY_ID, "Food", (short) 2);

        when(transactionRepository.findByIdAndUserIdAndIsDeletedFalse(TRANSACTION_ID, USER_ID))
                .thenReturn(Optional.of(existing));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));

        // When
        TransactionDto result = transactionService.getTransaction(USER_ID, TRANSACTION_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TRANSACTION_ID);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getCategoryName()).isEqualTo("Food");
    }

    @Test
    @DisplayName("getTransaction - throws when transaction not found")
    void getTransaction_notFound_throws() {
        // Given
        when(transactionRepository.findByIdAndUserIdAndIsDeletedFalse(TRANSACTION_ID, USER_ID))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> transactionService.getTransaction(USER_ID, TRANSACTION_ID))
                .isInstanceOf(BusinessException.class);
    }

    // ========== listTransactions ==========

    @Test
    @DisplayName("listTransactions - no filters returns all user transactions")
    void listTransactions_noFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Transaction t1 = createTransaction(1L, USER_ID, new BigDecimal("100.00"), (short) 1, CATEGORY_ID, LocalDate.of(2025, 1, 15));
        Transaction t2 = createTransaction(2L, USER_ID, new BigDecimal("50.00"), (short) 2, CATEGORY_ID, LocalDate.of(2025, 1, 14));
        Page<Transaction> page = new PageImpl<>(List.of(t1, t2), pageable, 2);
        Category category = createCategory(CATEGORY_ID, "Food", (short) 2);

        when(transactionRepository.findByUserIdAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(USER_ID, pageable))
                .thenReturn(page);
        when(categoryRepository.findAllById(List.of(CATEGORY_ID))).thenReturn(List.of(category));

        // When
        PageResponse<TransactionDto> result = transactionService.listTransactions(USER_ID, null, null, null, pageable);

        // Then
        assertThat(result.getList()).hasSize(2);
        assertThat(result.getTotal()).isEqualTo(2);
        verify(transactionRepository).findByUserIdAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(USER_ID, pageable);
    }

    @Test
    @DisplayName("listTransactions - with type filter")
    void listTransactions_withType() {
        // Given
        Short expenseType = 2;
        Pageable pageable = PageRequest.of(0, 10);
        Transaction t1 = createTransaction(1L, USER_ID, new BigDecimal("50.00"), expenseType, CATEGORY_ID, LocalDate.of(2025, 1, 15));
        Page<Transaction> page = new PageImpl<>(List.of(t1), pageable, 1);
        Category category = createCategory(CATEGORY_ID, "Food", expenseType);

        when(transactionRepository.findByUserIdAndTypeAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(USER_ID, expenseType, pageable))
                .thenReturn(page);
        when(categoryRepository.findAllById(List.of(CATEGORY_ID))).thenReturn(List.of(category));

        // When
        PageResponse<TransactionDto> result = transactionService.listTransactions(USER_ID, expenseType, null, null, pageable);

        // Then
        assertThat(result.getList()).hasSize(1);
        verify(transactionRepository).findByUserIdAndTypeAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(USER_ID, expenseType, pageable);
    }

    @Test
    @DisplayName("listTransactions - with date range filter")
    void listTransactions_withDateRange() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        Transaction t1 = createTransaction(1L, USER_ID, new BigDecimal("50.00"), (short) 2, CATEGORY_ID, LocalDate.of(2025, 1, 15));
        Page<Transaction> page = new PageImpl<>(List.of(t1), pageable, 1);
        Category category = createCategory(CATEGORY_ID, "Food", (short) 2);

        when(transactionRepository.findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, startDate, endDate, pageable)).thenReturn(page);
        when(categoryRepository.findAllById(List.of(CATEGORY_ID))).thenReturn(List.of(category));

        // When
        PageResponse<TransactionDto> result = transactionService.listTransactions(USER_ID, null, startDate, endDate, pageable);

        // Then
        assertThat(result.getList()).hasSize(1);
        verify(transactionRepository).findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, startDate, endDate, pageable);
    }

    @Test
    @DisplayName("listTransactions - with type and date range filters")
    void listTransactions_withTypeAndDateRange() {
        // Given
        Short expenseType = 2;
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        Transaction t1 = createTransaction(1L, USER_ID, new BigDecimal("50.00"), expenseType, CATEGORY_ID, LocalDate.of(2025, 1, 15));
        Page<Transaction> page = new PageImpl<>(List.of(t1), pageable, 1);
        Category category = createCategory(CATEGORY_ID, "Food", expenseType);

        when(transactionRepository.findByUserIdAndTypeAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, expenseType, startDate, endDate, pageable)).thenReturn(page);
        when(categoryRepository.findAllById(List.of(CATEGORY_ID))).thenReturn(List.of(category));

        // When
        PageResponse<TransactionDto> result = transactionService.listTransactions(USER_ID, expenseType, startDate, endDate, pageable);

        // Then
        assertThat(result.getList()).hasSize(1);
        verify(transactionRepository).findByUserIdAndTypeAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, expenseType, startDate, endDate, pageable);
    }

    // ========== getMonthlyTransactions ==========

    @Test
    @DisplayName("getMonthlyTransactions - returns transactions for specified month")
    void getMonthlyTransactions_success() {
        // Given
        int year = 2025;
        int month = 1;
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        Transaction t1 = createTransaction(1L, USER_ID, new BigDecimal("100.00"), (short) 1, CATEGORY_ID, LocalDate.of(2025, 1, 10));
        Transaction t2 = createTransaction(2L, USER_ID, new BigDecimal("50.00"), (short) 2, CATEGORY_ID, LocalDate.of(2025, 1, 20));
        Category category = createCategory(CATEGORY_ID, "Food", (short) 2);

        when(transactionRepository.findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, startDate, endDate)).thenReturn(List.of(t1, t2));
        when(categoryRepository.findAllById(List.of(CATEGORY_ID))).thenReturn(List.of(category));

        // When
        List<TransactionDto> result = transactionService.getMonthlyTransactions(USER_ID, year, month);

        // Then
        assertThat(result).hasSize(2);
        verify(transactionRepository).findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, startDate, endDate);
    }

    // ========== getRecentTransactions ==========

    @Test
    @DisplayName("getRecentTransactions - returns top 10 recent transactions")
    void getRecentTransactions_success() {
        // Given
        Transaction t1 = createTransaction(1L, USER_ID, new BigDecimal("100.00"), (short) 1, CATEGORY_ID, LocalDate.of(2025, 1, 15));
        Category category = createCategory(CATEGORY_ID, "Salary", (short) 1);

        when(transactionRepository.findTop10ByUserIdAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(USER_ID))
                .thenReturn(List.of(t1));
        when(categoryRepository.findAllById(List.of(CATEGORY_ID))).thenReturn(List.of(category));

        // When
        List<TransactionDto> result = transactionService.getRecentTransactions(USER_ID);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryName()).isEqualTo("Salary");
        verify(transactionRepository).findTop10ByUserIdAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(USER_ID);
    }

    // ========== enrichWithCategories (empty list) ==========

    @Test
    @DisplayName("enrichWithCategories - returns empty list for empty input")
    void enrichWithCategories_emptyList() {
        // Given
        when(transactionRepository.findTop10ByUserIdAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(USER_ID))
                .thenReturn(Collections.emptyList());

        // When
        List<TransactionDto> result = transactionService.getRecentTransactions(USER_ID);

        // Then
        assertThat(result).isEmpty();
        verify(categoryRepository, never()).findAllById(any());
    }
}
