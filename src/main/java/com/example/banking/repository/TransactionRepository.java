package com.example.banking.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.banking.model.TransactionEntry;

public interface TransactionRepository extends JpaRepository<TransactionEntry, UUID> {
	List<TransactionEntry> findByAccount_IdOrderByCreatedAtDesc(UUID accountId);
}
