package com.moneytracker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferToSavingsRequest {
    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private Long savingsId;
}
