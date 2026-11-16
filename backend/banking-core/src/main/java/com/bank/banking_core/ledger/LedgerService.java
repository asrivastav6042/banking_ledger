package com.bank.banking_core.ledger;

import com.bank.banking_core.entity.LedgerEntryEntity;
import com.bank.banking_core.enums.TransactionType;
import com.bank.banking_core.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerRepository ledgerRepository;

    @Transactional
    public LedgerEntryEntity recordEntry(String transactionReference,
                                         Long accountId,
                                         TransactionType transactionType,
                                         BigDecimal amount,
                                         BigDecimal balanceAfter,
                                         String description) {
        log.info("Creating ledger entry: ref={}, accountId={}, type={}, amount={}, balanceAfter={}",
                transactionReference, accountId, transactionType, amount, balanceAfter);

        LedgerEntryEntity entry = LedgerEntryEntity.builder()
                .transactionReference(transactionReference)
                .accountId(accountId)
                .transactionType(transactionType)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .description(description)
                .build();

        return ledgerRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntryEntity> getEntriesForAccount(Long accountId) {
        return ledgerRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }
}
