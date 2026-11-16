package com.bank.banking_core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking Core API")
                        .description("Core Banking System with Distributed Locking (Redisson), Ledgering, and Event Streaming (Kafka)")
                        .version("1.0.0")
                        .contact(new Contact().name("Engineering Team").email("engineering@bank.com")));
    }
}
