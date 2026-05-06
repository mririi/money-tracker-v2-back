package com.moneytracker.service;

import com.moneytracker.dto.SavingsRequest;
import com.moneytracker.entity.Savings;
import com.moneytracker.entity.User;
import com.moneytracker.repository.SavingsRepository;
import com.moneytracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SavingsService {

    @Autowired
    private SavingsRepository savingsRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Savings> getAllSavings() {
        Long userId = getCurrentUserId();
        return savingsRepository.findByUserId(userId);
    }

    public Savings getSavingsById(Long id) {
        Long userId = getCurrentUserId();
        Savings savings = savingsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Savings not found"));
        if (!savings.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return savings;
    }

    public Savings createSavings(SavingsRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Savings savings = Savings.builder()
                .name(request.getName())
                .description(request.getDescription())
                .currentAmount(request.getCurrentAmount())
                .targetAmount(request.getTargetAmount())
                .user(user)
                .build();
        return savingsRepository.save(savings);
    }

    public Savings updateSavings(Long id, SavingsRequest request) {
        Savings savings = getSavingsById(id);
        savings.setName(request.getName());
        savings.setDescription(request.getDescription());
        savings.setCurrentAmount(request.getCurrentAmount());
        savings.setTargetAmount(request.getTargetAmount());
        return savingsRepository.save(savings);
    }

    public void deleteSavings(Long id) {
        Savings savings = getSavingsById(id);
        savingsRepository.delete(savings);
    }

    private Long getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
