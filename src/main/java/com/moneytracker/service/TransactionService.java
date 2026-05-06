package com.moneytracker.service;

import com.moneytracker.dto.TransactionRequest;
import com.moneytracker.entity.*;
import com.moneytracker.repository.BudgetRepository;
import com.moneytracker.repository.TransactionRepository;
import com.moneytracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    public List<Transaction> getAllTransactions() {
        Long userId = getCurrentUserId();
        return transactionRepository.findByUserIdOrderByDateDesc(userId);
    }

    public Transaction getTransactionById(Long id) {
        Long userId = getCurrentUserId();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return transaction;
    }

    @Transactional
    public Transaction createTransaction(TransactionRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be positive");
        }

        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Transaction transaction = Transaction.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .user(user)
                .build();

        if (request.getBudgetId() != null) {
            Budget budget = budgetRepository.findById(request.getBudgetId())
                    .orElseThrow(() -> new RuntimeException("Budget not found"));
            if (!budget.getUser().getId().equals(userId)) {
                throw new RuntimeException("Unauthorized");
            }
            transaction.setBudget(budget);

            if (request.getType() == TransactionType.EXPENSE) {
                BigDecimal newSpent = budget.getSpentAmount().add(request.getAmount());
                if (newSpent.compareTo(budget.getTotalAmount()) > 0) {
                    throw new RuntimeException("Expense exceeds remaining budget");
                }
                budget.setSpentAmount(newSpent);
                budget.calculateRemaining();
                budgetRepository.save(budget);
            }
        }

        if (request.getType() == TransactionType.INCOME) {
            user.setBalance(user.getBalance().add(request.getAmount()));
            userRepository.save(user);
        }

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = getTransactionById(id);

        if (transaction.getBudget() != null && transaction.getType() == TransactionType.EXPENSE) {
            Budget budget = transaction.getBudget();
            budget.setSpentAmount(budget.getSpentAmount().subtract(transaction.getAmount()));
            budget.calculateRemaining();
            budgetRepository.save(budget);
        }

        if (transaction.getType() == TransactionType.INCOME) {
            User user = transaction.getUser();
            user.setBalance(user.getBalance().subtract(transaction.getAmount()));
            userRepository.save(user);
        }

        transactionRepository.delete(transaction);
    }

    private Long getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
