package com.finance.planner.accounting.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    @Min(value = 1, message = "Type must be 1 (income) or 2 (expense)")
    @Max(value = 2, message = "Type must be 1 (income) or 2 (expense)")
    private Short type;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    @Size(max = 50, message = "Payment method cannot exceed 50 characters")
    private String paymentMethod;

    @Size(max = 100, message = "Merchant cannot exceed 100 characters")
    private String merchant;
}
