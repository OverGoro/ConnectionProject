package com.service.auth.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.connection.client.model.ClientDTO;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("integrationtest")
@TestPropertySource(locations = "classpath:application-integrationtest.properties", properties = {
        "spring.config.location=classpath:application-integrationtest.properties",
        "spring.config.name=application-integrationtest"
})
@Slf4j
public abstract class BaseE2ETest {
    @Autowired
    private Environment environment;

    @LocalServerPort
    protected int port;

    protected TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    protected KafkaTemplate<String, Object> kafkaTemplate;

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
        log.info("Kafka servers: {}", environment.getProperty("spring.kafka.bootstrap-servers"));
        log.info("Active profiles: {}", Arrays.toString(environment.getActiveProfiles()));
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1";
    }

    @AfterEach
    void tearDown() {
    }

    protected void cleanupClientData(String email) {
        try {
            log.info("Cleaning up data for email: {}", email);

            // Сначала удаляем refresh tokens из схемы access
            String deleteTokensSql = "DELETE FROM \"access\".refresh_token WHERE client_uid IN (SELECT uid FROM core.client WHERE email = :email)";
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
                String deleteDeviceTokensSql = "DELETE FROM \"access\".device_token WHERE device_uid IN (SELECT uid FROM core.device WHERE client_uid IN (SELECT uid FROM core.client WHERE email = :email))";
                refreshTokenJdbcTemplate.update(deleteDeviceTokensSql, Map.of("email", email));

                // Очистка devices
                String deleteDevicesSql = "DELETE FROM core.device WHERE client_uid IN (SELECT uid FROM core.client WHERE email = :email)";
                clientJdbcTemplate.update(deleteDevicesSql, Map.of("email", email));

                // Очистка connection schemes
                String deleteSchemesSql = "DELETE FROM processing.connection_scheme WHERE client_uid IN (SELECT uid FROM core.client WHERE email = :email)";
                clientJdbcTemplate.update(deleteSchemesSql, Map.of("email", email));

            } catch (Exception e) {
                log.debug("Optional cleanup failed (tables might not exist): {}", e.getMessage());
            }

        } catch (Exception e) {
            log.warn("Cleanup warning for email {}: {}", email, e.getMessage());
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