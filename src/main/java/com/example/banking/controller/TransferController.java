package com.example.banking.controller;

import com.example.banking.dto.TransactionResponse;
import com.example.banking.dto.TransferRequest;
import com.example.banking.service.BankingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
	private final BankingService bankingService;

	public TransferController(BankingService bankingService) {
		this.bankingService = bankingService;
	}

	@PostMapping
	public List<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
		return bankingService.transfer(request.fromAccountId(), request.toAccountId(), request.amount());
	}
}
