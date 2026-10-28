package com.example.banking.dto;

import com.example.banking.model.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
		UUID id,
		UUID accountId,
		TransactionType type,
		BigDecimal amount,
		Instant createdAt,
		UUID referenceId) {
}
