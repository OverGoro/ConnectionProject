// package com.connection.service.auth.config.transaction;

// import java.util.UUID;

// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.transaction.jta.JtaTransactionManager;

// import com.atomikos.icatch.jta.UserTransactionManager;

// import jakarta.transaction.SystemException;

// @Configuration
// @ConditionalOnProperty(name = "app.transaction.mode", havingValue = "atomikos")
// public class TransactionConfig {

//     @Bean
//     public UserTransactionManager userTransactionManager() throws SystemException {
//         setPropertyIfNotExists("com.atomikos.icatch.log_base_name",
//                 "atomikos-tm-" + UUID.randomUUID().toString().substring(0, 8));
//         setPropertyIfNotExists("com.atomikos.icatch.log_base_dir", "./logs");
//         setPropertyIfNotExists("com.atomikos.icatch.tm_unique_name",
//                 "tm-" + UUID.randomUUID().toString().substring(0, 8));

//         setPropertyIfNotExists("com.atomikos.icatch.oltp_max_retries", "3"); // Максимум 3 попытки
//         setPropertyIfNotExists("com.atomikos.icatch.oltp_retry_interval", "250"); // 5 секунд между попытками
//         setPropertyIfNotExists("com.atomikos.icatch.max_timeout", "1000"); // 30 секунд общий таймаут
//         setPropertyIfNotExists("com.atomikos.icatch.default_jta_timeout", "300"); // 10 секунд на транзакцию
    

//         UserTransactionManager manager = new UserTransactionManager();

//         manager.setForceShutdown(true);
//         manager.setTransactionTimeout(1);

//         return manager;
//     }

//     @Bean(name = "atomicosTransactionManager")
//     public JtaTransactionManager jtaTransactiojnManager() throws SystemException {
//         JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
//         jtaTransactionManager.setTransactionManager(userTransactionManager());
//         jtaTransactionManager.setDefaultTimeout(1);
//         return jtaTransactionManager;
//     }

//     /**
//      * Устанавливает системное свойство только если оно еще не было установлено
//      * 
//      * @param key   ключ свойства
//      * @param value значение свойства
//      */
//     private void setPropertyIfNotExists(String key, String value) {
//         if (System.getProperty(key) == null) {
//             System.setProperty(key, value);
//             System.out.println("Set property: " + key + " = " + value);
//         } else {
//             System.out.println("Property already set: " + key + " = " + System.getProperty(key));
//         }
//     }
// }