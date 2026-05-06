package com.moneytracker.dto;

import com.moneytracker.entity.BudgetCategory;
import com.moneytracker.entity.RecurringFrequency;
import com.moneytracker.entity.TransactionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecurringTransactionRequest {
    @NotBlank
    private String description;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private TransactionType type;

    @NotNull
    private BudgetCategory category;

    @NotNull
    private RecurringFrequency frequency;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull
    @Min(1)
    @Max(31)
    private Integer dayOfMonth;

    private Long budgetId;
}
