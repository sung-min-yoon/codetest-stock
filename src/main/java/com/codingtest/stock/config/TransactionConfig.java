package com.codingtest.stock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableTransactionManagement
public class TransactionConfig implements TransactionManagementConfigurer {

    private final PlatformTransactionManager transactionManager;

    public TransactionConfig(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return transactionManager;
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(transactionManager);
    }
}