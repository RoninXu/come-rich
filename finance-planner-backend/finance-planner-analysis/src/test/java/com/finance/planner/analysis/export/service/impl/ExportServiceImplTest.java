package com.finance.planner.analysis.export.service.impl;

import com.finance.planner.accounting.entity.Category;
import com.finance.planner.accounting.entity.Transaction;
import com.finance.planner.accounting.repository.CategoryRepository;
import com.finance.planner.accounting.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExportServiceImpl Unit Tests")
class ExportServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ExportServiceImpl exportService;

    private static final Long USER_ID = 1L;

    private Transaction createTransaction(Long id, Short type, BigDecimal amount, Long categoryId, LocalDate date) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setUserId(USER_ID);
        t.setType(type);
        t.setAmount(amount);
        t.setCategoryId(categoryId);
        t.setTransactionDate(date);
        t.setDescription("Test transaction");
        t.setMerchant("Test merchant");
        t.setPaymentMethod("微信支付");
        t.setIsDeleted(false);
        return t;
    }

    private Category createCategory(Long id, String name, Short type) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        c.setType(type);
        c.setIcon("icon");
        c.setColor("#333");
        return c;
    }

    @Test
    @DisplayName("exportTransactionsToExcel - returns valid Excel bytes")
    void exportTransactionsToExcel_success() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        Transaction t1 = createTransaction(1L, (short) 2, new BigDecimal("100.50"), 10L, LocalDate.of(2026, 1, 5));
        Transaction t2 = createTransaction(2L, (short) 1, new BigDecimal("5000.00"), 20L, LocalDate.of(2026, 1, 10));

        Category c1 = createCategory(10L, "餐饮", (short) 2);
        Category c2 = createCategory(20L, "工资", (short) 1);

        when(transactionRepository
                .findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                        USER_ID, start, end))
                .thenReturn(List.of(t1, t2));
        when(categoryRepository.findAllById(anySet())).thenReturn(List.of(c1, c2));

        byte[] result = exportService.exportTransactionsToExcel(USER_ID, start, end, null, null);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
        // XLSX files start with PK (ZIP signature)
        assertThat(result[0]).isEqualTo((byte) 0x50);
        assertThat(result[1]).isEqualTo((byte) 0x4B);
    }

    @Test
    @DisplayName("exportTransactionsToCsv - returns valid CSV bytes with BOM")
    void exportTransactionsToCsv_success() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        Transaction t1 = createTransaction(1L, (short) 2, new BigDecimal("100.50"), 10L, LocalDate.of(2026, 1, 5));
        Category c1 = createCategory(10L, "餐饮", (short) 2);

        when(transactionRepository
                .findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                        USER_ID, start, end))
                .thenReturn(List.of(t1));
        when(categoryRepository.findAllById(anySet())).thenReturn(List.of(c1));

        byte[] result = exportService.exportTransactionsToCsv(USER_ID, start, end, null, null);

        assertThat(result).isNotNull();
        String csv = new String(result, java.nio.charset.StandardCharsets.UTF_8);
        // Check BOM
        assertThat(csv).startsWith("\uFEFF");
        // Check header
        assertThat(csv).contains("日期");
        assertThat(csv).contains("金额");
        // Check data
        assertThat(csv).contains("100.50");
        assertThat(csv).contains("餐饮");
    }

    @Test
    @DisplayName("exportMonthlyReport - returns valid multi-sheet Excel")
    void exportMonthlyReport_success() {
        Transaction t1 = createTransaction(1L, (short) 2, new BigDecimal("200"), 10L, LocalDate.of(2026, 2, 5));
        Transaction t2 = createTransaction(2L, (short) 1, new BigDecimal("8000"), 20L, LocalDate.of(2026, 2, 1));
        Category c1 = createCategory(10L, "餐饮", (short) 2);
        Category c2 = createCategory(20L, "工资", (short) 1);

        when(transactionRepository
                .findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                        eq(USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(t1, t2));
        when(categoryRepository.findAllById(anySet())).thenReturn(List.of(c1, c2));

        byte[] result = exportService.exportMonthlyReport(USER_ID, 2026, 2);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    @DisplayName("exportTransactionsToExcel - handles empty data")
    void exportTransactionsToExcel_emptyData() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        when(transactionRepository
                .findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                        USER_ID, start, end))
                .thenReturn(Collections.emptyList());

        byte[] result = exportService.exportTransactionsToExcel(USER_ID, start, end, null, null);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    @DisplayName("exportAnnualReport - returns valid annual report Excel")
    void exportAnnualReport_success() {
        Transaction t1 = createTransaction(1L, (short) 2, new BigDecimal("500"), 10L, LocalDate.of(2026, 3, 15));
        Category c1 = createCategory(10L, "餐饮", (short) 2);

        when(transactionRepository
                .findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                        eq(USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(t1));
        when(categoryRepository.findAllById(anySet())).thenReturn(List.of(c1));

        byte[] result = exportService.exportAnnualReport(USER_ID, 2026);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }
}
