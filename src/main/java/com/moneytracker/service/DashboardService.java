package com.moneytracker.service;

import com.moneytracker.dto.DashboardStats;
import com.moneytracker.entity.*;
import com.moneytracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private SavingsRepository savingsRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public DashboardStats getStats() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Budget> budgets = budgetRepository.findByUserId(userId);
        BigDecimal totalBudget = budgets.stream()
                .map(Budget::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSpent = budgets.stream()
                .map(Budget::getSpentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Savings> savings = savingsRepository.findByUserId(userId);
        BigDecimal totalSavings = savings.stream()
                .map(Savings::getCurrentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Plan> plans = planRepository.findByUserId(userId);
        BigDecimal totalPlansTarget = plans.stream()
                .map(Plan::getTargetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPlansSaved = plans.stream()
                .map(Plan::getSavedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long activePlans = plans.stream().filter(p -> p.getStatus() == PlanStatus.ACTIVE).count();
        long completedPlans = plans.stream().filter(p -> p.getStatus() == PlanStatus.COMPLETED).count();

        long transactionsCount = transactionRepository.countByUserId(userId);

        return DashboardStats.builder()
                .totalBudget(totalBudget)
                .totalSpent(totalSpent)
                .totalSavings(totalSavings)
                .totalPlansTarget(totalPlansTarget)
                .totalPlansSaved(totalPlansSaved)
                .activePlansCount(activePlans)
                .completedPlansCount(completedPlans)
                .budgetsCount(budgets.size())
                .transactionsCount(transactionsCount)
                .balance(user.getBalance())
                .build();
    }

    private Long getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
