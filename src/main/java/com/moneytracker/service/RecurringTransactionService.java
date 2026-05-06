package com.moneytracker.service;

import com.moneytracker.dto.RecurringTransactionRequest;
import com.moneytracker.entity.*;
import com.moneytracker.repository.BudgetRepository;
import com.moneytracker.repository.RecurringTransactionRepository;
import com.moneytracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecurringTransactionService {

    @Autowired
    private RecurringTransactionRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    public List<RecurringTransaction> getAll() {
        Long userId = getCurrentUserId();
        return repository.findByUserId(userId);
    }

    public RecurringTransaction getById(Long id) {
        Long userId = getCurrentUserId();
        RecurringTransaction rt = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recurring transaction not found"));
        if (!rt.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return rt;
    }

    public RecurringTransaction create(RecurringTransactionRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RecurringTransaction rt = RecurringTransaction.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .frequency(request.getFrequency())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .dayOfMonth(request.getDayOfMonth())
                .active(true)
                .user(user)
                .build();

        if (request.getBudgetId() != null) {
            Budget budget = budgetRepository.findById(request.getBudgetId())
                    .orElseThrow(() -> new RuntimeException("Budget not found"));
            if (!budget.getUser().getId().equals(userId)) {
                throw new RuntimeException("Unauthorized");
            }
            rt.setBudget(budget);
        }

        return repository.save(rt);
    }

    public RecurringTransaction update(Long id, RecurringTransactionRequest request) {
        RecurringTransaction rt = getById(id);
        rt.setDescription(request.getDescription());
        rt.setAmount(request.getAmount());
        rt.setType(request.getType());
        rt.setCategory(request.getCategory());
        rt.setFrequency(request.getFrequency());
        rt.setStartDate(request.getStartDate());
        rt.setEndDate(request.getEndDate());
        rt.setDayOfMonth(request.getDayOfMonth());

        if (request.getBudgetId() != null) {
            Budget budget = budgetRepository.findById(request.getBudgetId())
                    .orElseThrow(() -> new RuntimeException("Budget not found"));
            rt.setBudget(budget);
        } else {
            rt.setBudget(null);
        }

        return repository.save(rt);
    }

    public void delete(Long id) {
        RecurringTransaction rt = getById(id);
        repository.delete(rt);
    }

    public RecurringTransaction toggleActive(Long id) {
        RecurringTransaction rt = getById(id);
        rt.setActive(!rt.getActive());
        return repository.save(rt);
    }

    private Long getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
