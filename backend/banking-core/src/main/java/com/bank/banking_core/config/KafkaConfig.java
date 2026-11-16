package com.bank.banking_core.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import com.bank.banking_core.constants.AppConstants;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic bankingTransactionsTopic() {
        return TopicBuilder.name(AppConstants.TOPIC_BANKING_TRANSACTIONS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
