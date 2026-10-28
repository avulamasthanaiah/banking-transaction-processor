package com.example.banking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {
	@Id
	private UUID id;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal balance;

	protected Account() {
	}

	public Account(UUID id, BigDecimal openingBalance) {
		this.id = id;
		this.balance = openingBalance;
	}

	public UUID getId() {
		return id;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void deposit(BigDecimal amount) {
		balance = balance.add(amount);
	}

	public void withdraw(BigDecimal amount) {
		if (balance.compareTo(amount) < 0) {
			throw new IllegalArgumentException("Insufficient funds");
		}
		balance = balance.subtract(amount);
	}
}
