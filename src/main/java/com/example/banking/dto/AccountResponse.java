package com.example.banking.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
		UUID id,
		BigDecimal balance) {
}
