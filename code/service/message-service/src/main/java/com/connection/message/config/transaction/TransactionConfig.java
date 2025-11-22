package com.connection.message.config.transaction;

import com.atomikos.icatch.jta.UserTransactionManager;
import jakarta.transaction.SystemException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.jta.JtaTransactionManager;

/** . */
@Configuration
public class TransactionConfig {

    @Bean
    UserTransactionManager userTransactionManager() throws SystemException {
        UserTransactionManager manager = new UserTransactionManager();
        manager.setTransactionTimeout(300);
        manager.setForceShutdown(true);
        return manager;
    }

    @Bean(name = "atomicosTransactionManager")
    JtaTransactionManager jtaTransactionManager() throws SystemException {
        JtaTransactionManager jtaTransactionManager =
                new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(userTransactionManager());
        jtaTransactionManager.setUserTransaction(userTransactionManager());
        return jtaTransactionManager;
    }
}
