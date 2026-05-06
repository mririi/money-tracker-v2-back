package com.moneytracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private BigDecimal totalBudget;
    private BigDecimal totalSpent;
    private BigDecimal totalSavings;
    private BigDecimal totalPlansTarget;
    private BigDecimal totalPlansSaved;
    private long activePlansCount;
    private long completedPlansCount;
    private long budgetsCount;
    private long transactionsCount;
    private BigDecimal balance;
}
