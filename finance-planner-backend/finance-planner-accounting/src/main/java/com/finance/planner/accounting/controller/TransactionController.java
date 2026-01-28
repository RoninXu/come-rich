package com.finance.planner.accounting.controller;

import com.finance.planner.accounting.dto.CreateTransactionRequest;
import com.finance.planner.accounting.dto.TransactionDto;
import com.finance.planner.accounting.dto.UpdateTransactionRequest;
import com.finance.planner.accounting.service.TransactionService;
import com.finance.planner.auth.entity.User;
import com.finance.planner.auth.repository.UserRepository;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.common.response.ApiResponse;
import com.finance.planner.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management endpoints")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a new transaction")
    public ApiResponse<TransactionDto> createTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateTransactionRequest request) {
        Long userId = getUserId(userDetails);
        TransactionDto result = transactionService.createTransaction(userId, request);
        return ApiResponse.success(result);
    }

    @GetMapping
    @Operation(summary = "List transactions with pagination and filters")
    public ApiResponse<PageResponse<TransactionDto>> listTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Transaction type: 1=income, 2=expense")
            @RequestParam(required = false) Short type,
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = getUserId(userDetails);
        // Convert to 0-based page index
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize);
        PageResponse<TransactionDto> result = transactionService.listTransactions(userId, type, startDate, endDate, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single transaction by ID")
    public ApiResponse<TransactionDto> getTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getUserId(userDetails);
        TransactionDto result = transactionService.getTransaction(userId, id);
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing transaction")
    public ApiResponse<TransactionDto> updateTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest request) {
        Long userId = getUserId(userDetails);
        TransactionDto result = transactionService.updateTransaction(userId, id, request);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction (soft delete)")
    public ApiResponse<Void> deleteTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getUserId(userDetails);
        transactionService.deleteTransaction(userId, id);
        return ApiResponse.success();
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent transactions (for dashboard)")
    public ApiResponse<List<TransactionDto>> getRecentTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        List<TransactionDto> result = transactionService.getRecentTransactions(userId);
        return ApiResponse.success(result);
    }

    @GetMapping("/monthly/{year}/{month}")
    @Operation(summary = "Get transactions for a specific month")
    public ApiResponse<List<TransactionDto>> getMonthlyTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int year,
            @PathVariable int month) {
        Long userId = getUserId(userDetails);
        List<TransactionDto> result = transactionService.getMonthlyTransactions(userId, year, month);
        return ApiResponse.success(result);
    }

    /**
     * Extract user ID from authenticated user
     */
    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
