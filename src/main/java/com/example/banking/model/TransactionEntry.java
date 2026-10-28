package com.example.banking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transaction_entries", indexes = {
		@Index(name = "idx_transaction_account_timestamp", columnList = "account_id, created_at") })
public class TransactionEntry {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TransactionType type;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private UUID referenceId;

	protected TransactionEntry() {
		
	}

	public TransactionEntry(Account account, TransactionType type, BigDecimal amount, UUID referenceId) {
		this.id = UUID.randomUUID();
		this.account = account;
		this.type = type;
		this.amount = amount;
		this.referenceId = referenceId;
		this.createdAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public UUID getAccountId() {
		return account.getId();
	}

	public TransactionType getType() {
		return type;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public UUID getReferenceId() {
		return referenceId;
	}
}
