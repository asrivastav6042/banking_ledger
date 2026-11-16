package com.bank.banking_core.event.producer;

import com.bank.banking_core.constants.AppConstants;
import com.bank.banking_core.event.model.BankingTransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTransactionEvent(BankingTransactionEvent event) {
        String key = String.valueOf(event.getAccountId());
        log.info("Publishing banking transaction event to Kafka [Topic: {}, Key: {}]: {}",
                AppConstants.TOPIC_BANKING_TRANSACTIONS, key, event);

        kafkaTemplate.send(AppConstants.TOPIC_BANKING_TRANSACTIONS, key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully published event {} with offset {}",
                                event.getEventId(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to publish event {}: {}", event.getEventId(), ex.getMessage(), ex);
                    }
                });
    }
}
