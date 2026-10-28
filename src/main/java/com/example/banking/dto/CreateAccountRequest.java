package com.example.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotNull
        @DecimalMin(value = "0.00", inclusive = true, message = "Opening balance cannot be negative")
        @Digits(integer = 17, fraction = 2, message = "Opening balance supports at most 2 decimal places")
        BigDecimal openingBalance
) {
}
