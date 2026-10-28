package com.example.banking;

import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.exception.ResourceNotFoundException;
import com.example.banking.model.TransactionType;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.service.BankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class BankingServiceIntegrationTest {
	@Autowired
	BankingService bankingService;
	@Autowired
	AccountRepository accountRepository;
	@Autowired
	TransactionRepository transactionRepository;

	@BeforeEach
	void cleanDatabase() {
		transactionRepository.deleteAll();
		accountRepository.deleteAll();
	}

	@Test
	void createsAccountWithOpeningBalance() {
		AccountResponse account = bankingService.createAccount(new BigDecimal("100.00"));
		assertThat(account.id()).isNotNull();
		assertThat(account.balance()).isEqualByComparingTo("100.00");
	}

	@Test
	void depositIncreasesBalanceAndCreatesLedgerEntry() {
		AccountResponse account = bankingService.createAccount(new BigDecimal("10.00"));

		TransactionResponse transaction = bankingService.deposit(account.id(), new BigDecimal("5.25"));

		assertThat(bankingService.getAccount(account.id()).balance()).isEqualByComparingTo("15.25");
		assertThat(transaction.type()).isEqualTo(TransactionType.DEPOSIT);
		assertThat(transaction.amount()).isEqualByComparingTo("5.25");
		assertThat(bankingService.getTransactions(account.id())).hasSize(1);
	}

	@Test
	void withdrawalReducesBalance() {
		AccountResponse account = bankingService.createAccount(new BigDecimal("20.00"));

		bankingService.withdraw(account.id(), new BigDecimal("7.50"));

		assertThat(bankingService.getAccount(account.id()).balance()).isEqualByComparingTo("12.50");
	}

	@Test
	void rejectsOverdraftWithoutChangingBalanceOrLedger() {
		AccountResponse account = bankingService.createAccount(new BigDecimal("5.00"));

		assertThatThrownBy(() -> bankingService.withdraw(account.id(), new BigDecimal("5.01")))
				.isInstanceOf(IllegalArgumentException.class).hasMessage("Insufficient funds");

		assertThat(bankingService.getAccount(account.id()).balance()).isEqualByComparingTo("5.00");
		assertThat(bankingService.getTransactions(account.id())).isEmpty();
	}

	@Test
	void transferMovesMoneyAndWritesBothLedgerEntriesWithSharedReference() {
		AccountResponse source = bankingService.createAccount(new BigDecimal("50.00"));
		AccountResponse destination = bankingService.createAccount(new BigDecimal("10.00"));

		var entries = bankingService.transfer(source.id(), destination.id(), new BigDecimal("12.00"));

		assertThat(bankingService.getAccount(source.id()).balance()).isEqualByComparingTo("38.00");
		assertThat(bankingService.getAccount(destination.id()).balance()).isEqualByComparingTo("22.00");
		assertThat(entries).hasSize(2);
		assertThat(entries).extracting(TransactionResponse::type).containsExactly(TransactionType.TRANSFER_OUT,
				TransactionType.TRANSFER_IN);
		assertThat(entries.get(0).referenceId()).isEqualTo(entries.get(1).referenceId());
	}

	@Test
	void failedTransferDoesNotChangeEitherAccountOrWriteLedgerEntries() {
		AccountResponse source = bankingService.createAccount(new BigDecimal("5.00"));
		AccountResponse destination = bankingService.createAccount(new BigDecimal("10.00"));

		assertThatThrownBy(() -> bankingService.transfer(source.id(), destination.id(), new BigDecimal("6.00")))
				.isInstanceOf(IllegalArgumentException.class).hasMessage("Insufficient funds");

		assertThat(bankingService.getAccount(source.id()).balance()).isEqualByComparingTo("5.00");
		assertThat(bankingService.getAccount(destination.id()).balance()).isEqualByComparingTo("10.00");
		assertThat(bankingService.getTransactions(source.id())).isEmpty();
		assertThat(bankingService.getTransactions(destination.id())).isEmpty();
	}

	@Test
	void rejectsZeroAndNegativeAmounts() {
		AccountResponse account = bankingService.createAccount(BigDecimal.ZERO);

		assertThatThrownBy(() -> bankingService.deposit(account.id(), BigDecimal.ZERO))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> bankingService.withdraw(account.id(), new BigDecimal("-1.00")))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void rejectsTransferToSameAccount() {
		AccountResponse account = bankingService.createAccount(new BigDecimal("20.00"));

		assertThatThrownBy(() -> bankingService.transfer(account.id(), account.id(), BigDecimal.ONE))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Source and destination accounts must be different");
	}

	@Test
	void unknownAccountReturnsNotFound() {
		assertThatThrownBy(() -> bankingService.getAccount(UUID.randomUUID()))
				.isInstanceOf(ResourceNotFoundException.class);
	}
}
