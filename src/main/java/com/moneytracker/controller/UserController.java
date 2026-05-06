package com.moneytracker.controller;

import com.moneytracker.dto.BalanceUpdateRequest;
import com.moneytracker.dto.TransferToSavingsRequest;
import com.moneytracker.entity.Savings;
import com.moneytracker.entity.User;
import com.moneytracker.repository.SavingsRepository;
import com.moneytracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SavingsRepository savingsRepository;

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance() {
        User user = getCurrentUser();
        Map<String, Object> result = new HashMap<>();
        result.put("balance", user.getBalance());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/balance")
    public ResponseEntity<?> updateBalance(@Valid @RequestBody BalanceUpdateRequest request) {
        User user = getCurrentUser();
        user.setBalance(request.getAmount());
        userRepository.save(user);
        Map<String, Object> result = new HashMap<>();
        result.put("balance", user.getBalance());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/transfer-to-savings")
    public ResponseEntity<?> transferToSavings(@Valid @RequestBody TransferToSavingsRequest request) {
        User user = getCurrentUser();

        if (user.getBalance().compareTo(request.getAmount()) < 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Insufficient balance"));
        }

        Savings savings = savingsRepository.findById(request.getSavingsId())
                .orElseThrow(() -> new RuntimeException("Savings not found"));

        if (!savings.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Unauthorized"));
        }

        user.setBalance(user.getBalance().subtract(request.getAmount()));
        savings.setCurrentAmount(savings.getCurrentAmount().add(request.getAmount()));

        userRepository.save(user);
        savingsRepository.save(savings);

        Map<String, Object> result = new HashMap<>();
        result.put("balance", user.getBalance());
        result.put("savings", savings);
        return ResponseEntity.ok(result);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        String username = ((UserDetails) principal).getUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
