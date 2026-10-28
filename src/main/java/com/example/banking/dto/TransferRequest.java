package com.example.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
		@NotNull UUID fromAccountId,
		@NotNull UUID toAccountId,
		@NotNull @DecimalMin(value = "0.00", inclusive = false, message = "Amount must be greater than zero")
		@Digits(integer = 17, fraction = 2, message = "Amount supports at most 2 decimal places") BigDecimal amount) {
}
