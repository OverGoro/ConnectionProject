package com.service.connectionscheme.config.transaction;

import com.atomikos.icatch.jta.UserTransactionManager;
import jakarta.transaction.SystemException;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.jta.JtaTransactionManager;

/** . */
@Configuration
public class TransactionConfig {
    @Bean
    UserTransactionManager userTransactionManager()
            throws SystemException {
        // Устанавливаем уникальное имя через системные свойства только если они еще не установлены
        setPropertyIfNotExists("com.atomikos.icatch.log_base_name",
                "atomikos-tm-" + UUID.randomUUID().toString().substring(0, 8));
        setPropertyIfNotExists("com.atomikos.icatch.log_base_dir", "./logs");
        setPropertyIfNotExists("com.atomikos.icatch.tm_unique_name",
                "tm-" + UUID.randomUUID().toString().substring(0, 8));

        UserTransactionManager manager = new UserTransactionManager();
        manager.setTransactionTimeout(300);
        manager.setForceShutdown(true);
        return manager;
    }

    @Bean(name = "atomicosTransactionManager")
    JtaTransactionManager jtaTransactionManager()
            throws SystemException {
        JtaTransactionManager jtaTransactionManager =
                new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(userTransactionManager());
        jtaTransactionManager.setUserTransaction(userTransactionManager());
        return jtaTransactionManager;
    }

    /**
     * Устанавливает системное свойство только если оно еще не было установлено.
     * 
     * @param key ключ свойства
     * @param value значение свойства
     */
    private void setPropertyIfNotExists(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
            System.out.println("Set property: " + key + " = " + value);
        } else {
            System.out.println("Property already set: " + key + " = "
                    + System.getProperty(key));
        }
    }
}
