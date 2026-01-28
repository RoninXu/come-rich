package com.finance.planner.accounting.service.impl;

import com.finance.planner.accounting.dto.CreateTransactionRequest;
import com.finance.planner.accounting.dto.TransactionDto;
import com.finance.planner.accounting.dto.UpdateTransactionRequest;
import com.finance.planner.accounting.entity.Category;
import com.finance.planner.accounting.entity.Transaction;
import com.finance.planner.accounting.repository.CategoryRepository;
import com.finance.planner.accounting.repository.TransactionRepository;
import com.finance.planner.accounting.service.TransactionService;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public TransactionDto createTransaction(Long userId, CreateTransactionRequest request) {
        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        // Validate category type matches transaction type
        if (!category.getType().equals(request.getType())) {
            throw new BusinessException(ErrorCode.INVALID_TRANSACTION_TYPE,
                    "Category type does not match transaction type");
        }

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategoryId(request.getCategoryId());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setMerchant(request.getMerchant());
        transaction.setIsDeleted(false);

        Transaction saved = transactionRepository.save(transaction);
        log.info("Created transaction {} for user {}", saved.getId(), userId);

        return TransactionDto.fromEntityWithCategory(saved,
                category.getName(), category.getIcon(), category.getColor());
    }

    @Override
    @Transactional
    public TransactionDto updateTransaction(Long userId, Long transactionId, UpdateTransactionRequest request) {
        Transaction transaction = transactionRepository.findByIdAndUserIdAndIsDeletedFalse(transactionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));

        // Update fields if provided
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getType() != null) {
            transaction.setType(request.getType());
        }
        if (request.getCategoryId() != null) {
            // Validate category exists
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

            // Validate category type matches transaction type
            Short transactionType = request.getType() != null ? request.getType() : transaction.getType();
            if (!category.getType().equals(transactionType)) {
                throw new BusinessException(ErrorCode.INVALID_TRANSACTION_TYPE,
                        "Category type does not match transaction type");
            }
            transaction.setCategoryId(request.getCategoryId());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getTransactionDate() != null) {
            transaction.setTransactionDate(request.getTransactionDate());
        }
        if (request.getPaymentMethod() != null) {
            transaction.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getMerchant() != null) {
            transaction.setMerchant(request.getMerchant());
        }

        Transaction saved = transactionRepository.save(transaction);
        log.info("Updated transaction {} for user {}", saved.getId(), userId);

        return enrichWithCategory(saved);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserIdAndIsDeletedFalse(transactionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));

        transaction.setIsDeleted(true);
        transactionRepository.save(transaction);
        log.info("Soft deleted transaction {} for user {}", transactionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDto getTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserIdAndIsDeletedFalse(transactionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));

        return enrichWithCategory(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionDto> listTransactions(Long userId, Short type,
            LocalDate startDate, LocalDate endDate, Pageable pageable) {

        Page<Transaction> page;

        if (type != null && startDate != null && endDate != null) {
            page = transactionRepository.findByUserIdAndTypeAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                    userId, type, startDate, endDate, pageable);
        } else if (type != null) {
            page = transactionRepository.findByUserIdAndTypeAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                    userId, type, pageable);
        } else if (startDate != null && endDate != null) {
            page = transactionRepository.findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                    userId, startDate, endDate, pageable);
        } else {
            page = transactionRepository.findByUserIdAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                    userId, pageable);
        }

        List<TransactionDto> dtos = enrichWithCategories(page.getContent());
        return PageResponse.of(dtos, page.getTotalElements(), pageable.getPageNumber() + 1, pageable.getPageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> getMonthlyTransactions(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                        userId, startDate, endDate);

        return enrichWithCategories(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> getRecentTransactions(Long userId) {
        List<Transaction> transactions = transactionRepository
                .findTop10ByUserIdAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(userId);

        return enrichWithCategories(transactions);
    }

    /**
     * Enrich a single transaction with category details
     */
    private TransactionDto enrichWithCategory(Transaction transaction) {
        Category category = categoryRepository.findById(transaction.getCategoryId())
                .orElse(null);

        if (category != null) {
            return TransactionDto.fromEntityWithCategory(transaction,
                    category.getName(), category.getIcon(), category.getColor());
        }
        return TransactionDto.fromEntity(transaction);
    }

    /**
     * Enrich multiple transactions with category details (batch)
     */
    private List<TransactionDto> enrichWithCategories(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return List.of();
        }

        // Get unique category IDs
        List<Long> categoryIds = transactions.stream()
                .map(Transaction::getCategoryId)
                .distinct()
                .collect(Collectors.toList());

        // Fetch all categories in one query
        Map<Long, Category> categoryMap = categoryRepository.findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        // Map transactions to DTOs with category details
        return transactions.stream()
                .map(t -> {
                    Category category = categoryMap.get(t.getCategoryId());
                    if (category != null) {
                        return TransactionDto.fromEntityWithCategory(t,
                                category.getName(), category.getIcon(), category.getColor());
                    }
                    return TransactionDto.fromEntity(t);
                })
                .collect(Collectors.toList());
    }
}
