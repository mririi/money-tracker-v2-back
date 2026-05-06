package com.moneytracker.config;

import com.moneytracker.entity.*;
import com.moneytracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User user = User.builder()
                    .username("demo")
                    .email("demo@moneytracker.com")
                    .password(passwordEncoder.encode("demo123"))
                    .build();
            user = userRepository.save(user);

            Budget budget1 = Budget.builder()
                    .name("Monthly Food")
                    .description("Groceries and dining out")
                    .totalAmount(new BigDecimal("500.00"))
                    .spentAmount(new BigDecimal("250.00"))
                    .category(BudgetCategory.FOOD)
                    .period(BudgetPeriod.MONTHLY)
                    .startDate(LocalDateTime.now().withDayOfMonth(1))
                    .endDate(LocalDateTime.now().withDayOfMonth(1).plusMonths(1).minusDays(1))
                    .user(user)
                    .build();
            budget1.calculateRemaining();
            budgetRepository.save(budget1);

            Budget budget2 = Budget.builder()
                    .name("Transport")
                    .description("Gas and public transport")
                    .totalAmount(new BigDecimal("200.00"))
                    .spentAmount(new BigDecimal("80.00"))
                    .category(BudgetCategory.TRANSPORT)
                    .period(BudgetPeriod.MONTHLY)
                    .startDate(LocalDateTime.now().withDayOfMonth(1))
                    .endDate(LocalDateTime.now().withDayOfMonth(1).plusMonths(1).minusDays(1))
                    .user(user)
                    .build();
            budget2.calculateRemaining();
            budgetRepository.save(budget2);

            Savings savings = Savings.builder()
                    .name("Emergency Fund")
                    .description("Rainy day savings")
                    .currentAmount(new BigDecimal("3000.00"))
                    .targetAmount(new BigDecimal("10000.00"))
                    .user(user)
                    .build();
            savingsRepository.save(savings);

            Plan plan = Plan.builder()
                    .name("New Laptop")
                    .description("MacBook Pro for development")
                    .targetAmount(new BigDecimal("2500.00"))
                    .savedAmount(new BigDecimal("1200.00"))
                    .targetDate(LocalDate.now().plusMonths(6))
                    .status(PlanStatus.ACTIVE)
                    .user(user)
                    .build();
            planRepository.save(plan);

            Transaction t1 = Transaction.builder()
                    .description("Grocery shopping")
                    .amount(new BigDecimal("120.50"))
                    .type(TransactionType.EXPENSE)
                    .category(BudgetCategory.FOOD)
                    .user(user)
                    .budget(budget1)
                    .build();
            transactionRepository.save(t1);

            Transaction t2 = Transaction.builder()
                    .description("Gas refill")
                    .amount(new BigDecimal("45.00"))
                    .type(TransactionType.EXPENSE)
                    .category(BudgetCategory.TRANSPORT)
                    .user(user)
                    .budget(budget2)
                    .build();
            transactionRepository.save(t2);
        }
    }
}
