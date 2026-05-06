package com.moneytracker.service;

import com.moneytracker.dto.BudgetRequest;
import com.moneytracker.entity.Budget;
import com.moneytracker.entity.User;
import com.moneytracker.repository.BudgetRepository;
import com.moneytracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Budget> getAllBudgets() {
        Long userId = getCurrentUserId();
        return budgetRepository.findByUserId(userId);
    }

    public Budget getBudgetById(Long id) {
        Long userId = getCurrentUserId();
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        if (!budget.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return budget;
    }

    public Budget createBudget(BudgetRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Budget budget = Budget.builder()
                .name(request.getName())
                .description(request.getDescription())
                .totalAmount(request.getTotalAmount())
                .spentAmount(java.math.BigDecimal.ZERO)
                .category(request.getCategory())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .user(user)
                .build();
        budget.calculateRemaining();
        return budgetRepository.save(budget);
    }

    public Budget updateBudget(Long id, BudgetRequest request) {
        Budget budget = getBudgetById(id);
        if (request.getTotalAmount().compareTo(budget.getSpentAmount()) < 0) {
            throw new RuntimeException("Total amount cannot be less than spent amount");
        }
        budget.setName(request.getName());
        budget.setDescription(request.getDescription());
        budget.setTotalAmount(request.getTotalAmount());
        budget.setCategory(request.getCategory());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.calculateRemaining();
        return budgetRepository.save(budget);
    }

    public void deleteBudget(Long id) {
        Budget budget = getBudgetById(id);
        budgetRepository.delete(budget);
    }

    private Long getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
