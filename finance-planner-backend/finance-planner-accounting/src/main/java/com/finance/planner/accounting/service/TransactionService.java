package com.finance.planner.accounting.service;

import com.finance.planner.accounting.dto.CreateTransactionRequest;
import com.finance.planner.accounting.dto.TransactionDto;
import com.finance.planner.accounting.dto.UpdateTransactionRequest;
import com.finance.planner.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    /**
     * Create a new transaction
     */
    TransactionDto createTransaction(Long userId, CreateTransactionRequest request);

    /**
     * Update an existing transaction
     */
    TransactionDto updateTransaction(Long userId, Long transactionId, UpdateTransactionRequest request);

    /**
     * Soft delete a transaction
     */
    void deleteTransaction(Long userId, Long transactionId);

    /**
     * Get a single transaction by ID
     */
    TransactionDto getTransaction(Long userId, Long transactionId);

    /**
     * List transactions with pagination and optional filters
     */
    PageResponse<TransactionDto> listTransactions(Long userId, Short type, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Get transactions for a specific month
     */
    List<TransactionDto> getMonthlyTransactions(Long userId, int year, int month);

    /**
     * Get recent transactions for dashboard
     */
    List<TransactionDto> getRecentTransactions(Long userId);
}
