package com.connection.gateway.config.transaction;

import com.atomikos.icatch.jta.UserTransactionManager;
import jakarta.transaction.SystemException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.jta.JtaTransactionManager;

/** . */
@Configuration
public class TransactionConfig {

    private static volatile boolean databaseChecked = false;
    private static final Object CHECK_LOCK = new Object();

    @Value("${transaction.database.host:localhost}")
    private String dbHost;
    @Value("${transaction.database.port:5432}")
    private int dbPort;

    @Bean
    UserTransactionManager userTransactionManager() throws SystemException {
        setPropertyIfNotExists("com.atomikos.icatch.log_base_name",
                "atomikos-tm-" + UUID.randomUUID().toString().substring(0, 8));
        setPropertyIfNotExists("com.atomikos.icatch.log_base_dir", "./logs");
        setPropertyIfNotExists("com.atomikos.icatch.tm_unique_name",
                "tm-" + UUID.randomUUID().toString().substring(0, 8));

        // Настройки для минимальных попыток переподключения
        setPropertyIfNotExists("com.atomikos.icatch.oltp_max_retries", "10");
        setPropertyIfNotExists("com.atomikos.icatch.oltp_retry_interval",
                "1000");
        setPropertyIfNotExists("com.atomikos.icatch.default_jta_timeout",
                "5000");

        // Выполняем проверку БД только один раз при создании первого бина
        performOneTimeDatabaseCheck();

        UserTransactionManager manager = new UserTransactionManager();
        manager.setTransactionTimeout(30);
        manager.setForceShutdown(true);
        return manager;
    }

    @Bean(name = "atomicosTransactionManager")
    JtaTransactionManager jtaTransactionManager() throws SystemException {
        // Проверка уже выполнена в userTransactionManager(), поэтому не повторяем

        JtaTransactionManager jtaTransactionManager =
                new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(userTransactionManager());
        jtaTransactionManager.setUserTransaction(userTransactionManager());
        jtaTransactionManager.setDefaultTimeout(5000);
        return jtaTransactionManager;
    }

    private void performOneTimeDatabaseCheck() {
        synchronized (CHECK_LOCK) {
            if (databaseChecked) {
                System.out.println(
                        "✓ Database check already performed - skipping");
                return;
            }

            System.out.println(
                    "=== PERFORMING ONE-TIME DATABASE AVAILABILITY CHECK ===");

            // Простая проверка доступности порта БД
            int timeout = 3000; // 3 секунды

            boolean isReachable = isPortReachable(dbHost, dbPort, timeout);

            if (!isReachable) {
                System.err.println("✗ CRITICAL: Database is not reachable at "
                        + dbHost + ":" + dbPort);
                System.err.println("=== APPLICATION WILL NOW EXIT ===");
                System.exit(1);
            } else {
                System.out.println("✓ Database port is reachable at " + dbHost
                        + ":" + dbPort);
            }

            databaseChecked = true;
            System.out.println("=== ONE-TIME DATABASE CHECK COMPLETED ===");
        }
    }

    /**
     * Проверяет доступность порта БД.
     */
    private boolean isPortReachable(String host, int port, int timeout) {
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), timeout);
            return true;
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Устанавливает системное свойство только если оно еще не было установлено.
     */
    private void setPropertyIfNotExists(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
            System.out.println("Set property: " + key + " = " + value);
        }
    }
}
