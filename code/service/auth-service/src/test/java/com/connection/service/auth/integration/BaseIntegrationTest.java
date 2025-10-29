package com.connection.service.auth.integration;

// import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
// import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import lombok.extern.slf4j.Slf4j;


@ActiveProfiles("integrationtest")
@TestPropertySource(locations = "classpath:application-integrationtest.properties", properties = {
        "spring.config.location=classpath:application-integrationtest.properties",
        "spring.config.name=application-integrationtest"
})
@TestMethodOrder(MethodOrderer.Random.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public abstract class BaseIntegrationTest {

    @Autowired
    protected Environment environment;

    protected TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    @Qualifier("ClientJdbcTemplate")
    protected NamedParameterJdbcTemplate clientJdbcTemplate;

    @Autowired
    @Qualifier("RefreshTokenJdbcTemplate")
    protected NamedParameterJdbcTemplate refreshTokenJdbcTemplate;

    protected String baseUrl;
    protected final Map<String, String> testData = new ConcurrentHashMap<>();

    @BeforeEach
    void checkConfig() {
        log.info("Active profiles: {}", Arrays.toString(environment.getActiveProfiles()));
        // log.info("Kafka servers: {}", environment.getProperty("spring.kafka.bootstrap-servers"));
        log.info("Database URLs: client={}, refresh-token={}", 
            environment.getProperty("app.datasource.client.xa-properties.url"),
            environment.getProperty("app.datasource.refresh-token.xa-properties.url"));
    }

    @AfterEach
    void tearDown() {
        // Базовая очистка, может быть переопределена в наследниках
        testData.clear();
    }

    protected void cleanupClientData(String email) {
        try {
            log.info("Cleaning up data for email: {}", email);

            // Сначала удаляем refresh tokens из схемы access
            String deleteTokensSql = "DELETE FROM \"access\".refresh_token WHERE client_id IN (SELECT uid FROM core.client WHERE email = :email)";
            int tokensDeleted = refreshTokenJdbcTemplate.update(deleteTokensSql, Map.of("email", email));
            if (tokensDeleted > 0) {
                log.info("Deleted {} refresh tokens for email: {}", tokensDeleted, email);
            }

            // Затем удаляем клиента из схемы core
            String deleteClientSql = "DELETE FROM core.client WHERE email = :email";
            int clientsDeleted = clientJdbcTemplate.update(deleteClientSql, Map.of("email", email));
            if (clientsDeleted > 0) {
                log.info("Deleted {} clients for email: {}", clientsDeleted, email);
            }

            // Дополнительная очистка связанных данных на всякий случай
            try {
                // Очистка device tokens
                String deleteDeviceTokensSql = "DELETE FROM \"access\".device_token WHERE device_uid IN (SELECT uid FROM core.device WHERE client_uuid IN (SELECT uid FROM core.client WHERE email = :email))";
                refreshTokenJdbcTemplate.update(deleteDeviceTokensSql, Map.of("email", email));

                // Очистка devices
                String deleteDevicesSql = "DELETE FROM core.device WHERE client_uuid IN (SELECT uid FROM core.client WHERE email = :email)";
                clientJdbcTemplate.update(deleteDevicesSql, Map.of("email", email));

                // Очистка connection schemes
                String deleteSchemesSql = "DELETE FROM processing.connection_scheme WHERE client_uid IN (SELECT uid FROM core.client WHERE email = :email)";
                clientJdbcTemplate.update(deleteSchemesSql, Map.of("email", email));

            } catch (Exception e) {
                log.debug("Optional cleanup failed (tables might not exist): {}", e.getMessage());
            }

        } catch (Exception e) {
            log.warn("Cleanup warning for email {}: {}", email, e);
        }
    }

    protected void sleep(long milliseconds) {
        try {
            log.debug("Sleeping for {} ms", milliseconds);
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Sleep interrupted", e);
        }
    }

    protected HttpEntity<Object> createHttpEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}