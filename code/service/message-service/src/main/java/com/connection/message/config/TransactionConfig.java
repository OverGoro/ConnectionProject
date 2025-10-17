package com.connection.message.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.jta.JtaTransactionManager;

import com.atomikos.icatch.jta.UserTransactionManager;

import jakarta.transaction.SystemException;

@Configuration
public class TransactionConfig {

    @Bean
    public UserTransactionManager userTransactionManager() throws SystemException{
        UserTransactionManager manager = new UserTransactionManager();
        manager.setTransactionTimeout(300);
        manager.setForceShutdown(true);
        return manager;
    }

    @Bean(name = "atomicosTransactionManager")
    public JtaTransactionManager jtaTransactionManager() throws SystemException {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(userTransactionManager());
        jtaTransactionManager.setUserTransaction(userTransactionManager());
        return jtaTransactionManager;
    }
}