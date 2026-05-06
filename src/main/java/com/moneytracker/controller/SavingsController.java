package com.moneytracker.controller;

import com.moneytracker.dto.SavingsRequest;
import com.moneytracker.entity.Savings;
import com.moneytracker.service.SavingsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/savings")
public class SavingsController {

    @Autowired
    private SavingsService savingsService;

    @GetMapping
    public ResponseEntity<List<Savings>> getAllSavings() {
        return ResponseEntity.ok(savingsService.getAllSavings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Savings> getSavingsById(@PathVariable Long id) {
        return ResponseEntity.ok(savingsService.getSavingsById(id));
    }

    @PostMapping
    public ResponseEntity<Savings> createSavings(@Valid @RequestBody SavingsRequest request) {
        return ResponseEntity.ok(savingsService.createSavings(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Savings> updateSavings(@PathVariable Long id, @Valid @RequestBody SavingsRequest request) {
        return ResponseEntity.ok(savingsService.updateSavings(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSavings(@PathVariable Long id) {
        savingsService.deleteSavings(id);
        return ResponseEntity.ok().build();
    }
}
