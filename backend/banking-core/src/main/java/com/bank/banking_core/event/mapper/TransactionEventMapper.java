package com.bank.banking_core.event.mapper;

import com.bank.banking_core.entity.LedgerEntryEntity;
import com.bank.banking_core.event.model.BankingTransactionEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TransactionEventMapper {

    public BankingTransactionEvent toEvent(LedgerEntryEntity ledgerEntry) {
        if (ledgerEntry == null) {
            return null;
        }
        return BankingTransactionEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .transactionReference(ledgerEntry.getTransactionReference())
                .accountId(ledgerEntry.getAccountId())
                .transactionType(ledgerEntry.getTransactionType())
                .amount(ledgerEntry.getAmount())
                .balanceAfter(ledgerEntry.getBalanceAfter())
                .description(ledgerEntry.getDescription())
                .timestamp(ledgerEntry.getCreatedAt())
                .build();
    }
}
