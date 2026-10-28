package com.example.banking.controller;

import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.AmountRequest;
import com.example.banking.dto.CreateAccountRequest;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.service.BankingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
	private final BankingService bankingService;

	public AccountController(BankingService bankingService) {
		this.bankingService = bankingService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
		return bankingService.createAccount(request.openingBalance());
	}

	@GetMapping("/{accountId}")
	public AccountResponse getAccount(@PathVariable UUID accountId) {
		return bankingService.getAccount(accountId);
	}

	@PostMapping("/{accountId}/deposits")
	public TransactionResponse deposit(@PathVariable UUID accountId, @Valid @RequestBody AmountRequest request) {
		return bankingService.deposit(accountId, request.amount());
	}

	@PostMapping("/{accountId}/withdrawals")
	public TransactionResponse withdraw(@PathVariable UUID accountId, @Valid @RequestBody AmountRequest request) {
		return bankingService.withdraw(accountId, request.amount());
	}

	@GetMapping("/{accountId}/transactions")
	public List<TransactionResponse> getTransactions(@PathVariable UUID accountId) {
		return bankingService.getTransactions(accountId);
	}
}
