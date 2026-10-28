package com.example.banking.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.exception.ResourceNotFoundException;
import com.example.banking.model.Account;
import com.example.banking.model.TransactionEntry;
import com.example.banking.model.TransactionType;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;

@Service
public class BankingService {
	private final AccountRepository accounts;
	private final TransactionRepository transactions;

	public BankingService(AccountRepository accounts, TransactionRepository transactions) {
		this.accounts = accounts;
		this.transactions = transactions;
	}

	@Transactional
	public AccountResponse createAccount(BigDecimal openingBalance) {
		if (openingBalance == null || openingBalance.signum() < 0) {
			throw new IllegalArgumentException("Opening balance cannot be negative");
		}
		if (openingBalance.scale() > 2) {
			throw new IllegalArgumentException("Opening balance supports at most 2 decimal places");
		}
		Account account = accounts.save(new Account(UUID.randomUUID(), openingBalance));
		return toResponse(account);
	}

	@Transactional(readOnly = true)
	public AccountResponse getAccount(UUID accountId) {
		return toResponse(findAccount(accountId));
	}

	@Transactional
	public TransactionResponse deposit(UUID accountId, BigDecimal amount) {
		validateAmount(amount);
		Account account = lockAccount(accountId);
		account.deposit(amount);
		TransactionEntry entry = transactions
				.save(new TransactionEntry(account, TransactionType.DEPOSIT, amount, UUID.randomUUID()));
		return toResponse(entry);
	}

	@Transactional
	public TransactionResponse withdraw(UUID accountId, BigDecimal amount) {
		validateAmount(amount);
		Account account = lockAccount(accountId);
		account.withdraw(amount);
		TransactionEntry entry = transactions
				.save(new TransactionEntry(account, TransactionType.WITHDRAWAL, amount, UUID.randomUUID()));
		return toResponse(entry);
	}

	@Transactional
	public List<TransactionResponse> transfer(UUID fromId, UUID toId, BigDecimal amount) {
		validateAmount(amount);
		if (fromId == null || toId == null) {
			throw new IllegalArgumentException("Source and destination account IDs are required");
		}
		if (fromId.equals(toId)) {
			throw new IllegalArgumentException("Source and destination accounts must be different");
		}

		// Lock in a stable order to reduce deadlock risk for opposite-direction
		// transfers.
		UUID firstId = fromId.compareTo(toId) < 0 ? fromId : toId;
		UUID secondId = fromId.compareTo(toId) < 0 ? toId : fromId;
		Account first = lockAccount(firstId);
		Account second = lockAccount(secondId);
		Account source = fromId.equals(firstId) ? first : second;
		Account destination = toId.equals(firstId) ? first : second;

		source.withdraw(amount); // If this fails, the transaction rolls back.
		destination.deposit(amount);

		UUID transferReference = UUID.randomUUID();
		TransactionEntry outgoing = transactions
				.save(new TransactionEntry(source, TransactionType.TRANSFER_OUT, amount, transferReference));
		TransactionEntry incoming = transactions
				.save(new TransactionEntry(destination, TransactionType.TRANSFER_IN, amount, transferReference));
		return List.of(toResponse(outgoing), toResponse(incoming));
	}

	@Transactional(readOnly = true)
	public List<TransactionResponse> getTransactions(UUID accountId) {
		if (!accounts.existsById(accountId)) {
			throw new ResourceNotFoundException("Account not found: " + accountId);
		}
		return transactions.findByAccount_IdOrderByCreatedAtDesc(accountId).stream().map(BankingService::toResponse)
				.toList();
	}

	private Account findAccount(UUID id) {
		return accounts.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
	}

	private Account lockAccount(UUID id) {
		return accounts.findByIdForUpdate(id)
				.orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
	}

	private static void validateAmount(BigDecimal amount) {
		if (amount == null || amount.signum() <= 0) {
			throw new IllegalArgumentException("Amount must be greater than zero");
		}
		if (amount.scale() > 2) {
			throw new IllegalArgumentException("Amount supports at most 2 decimal places");
		}
	}

	private static AccountResponse toResponse(Account account) {
		return new AccountResponse(account.getId(), account.getBalance());
	}

	private static TransactionResponse toResponse(TransactionEntry entry) {
		return new TransactionResponse(entry.getId(), entry.getAccountId(), entry.getType(), entry.getAmount(),
				entry.getCreatedAt(), entry.getReferenceId());
	}
}
