package com.bank.banking_core.repository;

import com.bank.banking_core.entity.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerRepository extends JpaRepository<LedgerEntryEntity, Long> {
    List<LedgerEntryEntity> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
