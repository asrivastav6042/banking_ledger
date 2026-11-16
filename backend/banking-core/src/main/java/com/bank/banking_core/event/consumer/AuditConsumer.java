package com.bank.banking_core.event.consumer;

import com.bank.banking_core.constants.AppConstants;
import com.bank.banking_core.event.model.BankingTransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditConsumer {

    @KafkaListener(
            topics = AppConstants.TOPIC_BANKING_TRANSACTIONS,
            groupId = "banking-audit-group"
    )
    public void consumeTransactionEvent(BankingTransactionEvent event) {
        log.info(">>> [AUDIT CONSUMER] Received Audit Event <<<");
        log.info("Event ID             : {}", event.getEventId());
        log.info("Transaction Ref      : {}", event.getTransactionReference());
        log.info("Account ID           : {}", event.getAccountId());
        log.info("Transaction Type     : {}", event.getTransactionType());
        log.info("Amount               : {}", event.getAmount());
        log.info("Balance After        : {}", event.getBalanceAfter());
        log.info("Description          : {}", event.getDescription());
        log.info("Timestamp            : {}", event.getTimestamp());
        log.info(">>> [AUDIT CONSUMER] Event Processed and Logged for Compliance <<<");
    }
}
