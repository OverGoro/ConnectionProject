// package com.connection.gateway.config.spring;

// import java.sql.Connection;
// import java.sql.SQLException;
// import java.sql.Statement;
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.Executors;
// import java.util.concurrent.ScheduledExecutorService;
// import java.util.concurrent.TimeUnit;

// import javax.sql.DataSource;

// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.stereotype.Component;

// import lombok.extern.slf4j.Slf4j;

// @Slf4j
// @Component
// public class DatabaseHealthMonitor {

// private final Map<String, DataSource> dataSources;
// private final Map<String, Integer> failureCounts = new ConcurrentHashMap<>();
// private final Map<String, Boolean> connectionStatus = new ConcurrentHashMap<>();
// private final int MAX_FAILURES = 10;
// private final long CHECK_INTERVAL = 1; // 30 секунд

// public DatabaseHealthMonitor(@Qualifier("ClientDataSource") DataSource clientDataSource,
// @Qualifier("RefreshTokenDataSource") DataSource refreshTokenDataSource,
// @Qualifier("BufferDataSource") DataSource bufferDataSource,
// @Qualifier("ConnectionSchemeDataSource") DataSource connectionSchemeDataSource,
// @Qualifier("deviceTokenDataSource") DataSource deviceTokenDataSource,
// @Qualifier("deviceAccessTokenDataSource") DataSource deviceAccessTokenDataSource,
// @Qualifier("DeviceDataSource") DataSource deviceDataSource,
// @Qualifier("MessageDataSource") DataSource messageDataSource) {
// this.dataSources = Map.of(
// "RefreshTokenDataSource", refreshTokenDataSource,
// "BufferDataSource", bufferDataSource,
// "ConnectionSchemeDataSource", connectionSchemeDataSource,
// "deviceTokenDataSource", deviceTokenDataSource,
// "deviceAccessTokenDataSource", deviceAccessTokenDataSource,
// "DeviceDataSource", deviceDataSource,
// "MessageDataSource", messageDataSource);

// initializeMonitoring();
// }

// private void checkAllConnections() {
// for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
// String dataSourceName = entry.getKey();
// DataSource dataSource = entry.getValue();

// boolean isAvailable = testConnection(dataSource);

// if (isAvailable) {
// // Сброс счетчика при успешном соединении
// failureCounts.put(dataSourceName, 0);
// connectionStatus.put(dataSourceName, true);
// log.info("Connection {} is available", dataSourceName);
// } else {
// // Увеличение счетчика при неудаче
// int failures = failureCounts.getOrDefault(dataSourceName, 0) + 1;
// failureCounts.put(dataSourceName, failures);
// connectionStatus.put(dataSourceName, false);

// log.warn("Connection {} failed {}/{} times", dataSourceName, failures, MAX_FAILURES);

// // Остановка приложения при достижении лимита
// if (failures >= MAX_FAILURES) {
// handleCriticalFailure(dataSourceName);
// }
// }
// }
// }

// private void handleCriticalFailure(String dataSourceName) {
// log.error("CRITICAL: DataSource {} 
// has failed {} times consecutively. Shutting down application.",
// dataSourceName, MAX_FAILURES);
// logCurrentStatus();
// System.exit(1);
// }

// private void logCurrentStatus() {
// log.info("=== Current Database Connection Status ===");
// connectionStatus.forEach((name, status) -> log.info("{}: {} (failures: {})",
// name,
// status ? "AVAILABLE" : "UNAVAILABLE",
// failureCounts.getOrDefault(name, 0)));
// log.info("==========================================");
// }

// private boolean testConnection(DataSource dataSource) {
// try (Connection connection = dataSource.getConnection();
// Statement statement = connection.createStatement()) {

// // Простой запрос для проверки соединения
// statement.executeQuery("SELECT 1");
// return true;

// } catch (SQLException e) {
// log.error("Database connection test failed: {}", e.getMessage());
// return false;
// }
// }

// private void initializeMonitoring() {
// ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
// scheduler.scheduleAtFixedRate(this::checkAllConnections, 
// 0, CHECK_INTERVAL, TimeUnit.MILLISECONDS);
// }
// }