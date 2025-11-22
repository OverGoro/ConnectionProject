package com.connection.client.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.connection.client.exception.ClientAlreadyExisistsException;
import com.connection.client.exception.ClientNotFoundException;
import com.connection.client.model.ClientBlm;
import com.connection.client.repository.ClientRepository;
import com.connection.client.repository.ClientRepositorySqlImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisplayName("Client Repository Sql Implementation Tests")
public class ClientRepositorySqlImplIntegrationTest extends BaseClientRepositoryIntegrationTest {

    private ClientRepository clientRepository;

    @BeforeEach
    void setUpRepository() {
        this.clientRepository = new ClientRepositorySqlImpl(jdbcTemplate);
    }

    @Test
    @DisplayName("Should add client successfully")
    void shouldAddClientSuccessfully() {
        // Given
        ClientBlm testClient = ClientBlm.builder()
                .uid(testClientUid)
                .email(testEmail)
                .username(testUsername)
                .password(testPassword)
                .birthDate(new Date())
                .build();

        // When
        clientRepository.add(testClient);

        // Then
        ClientBlm foundClient = clientRepository.findByUid(testClientUid);

        assertThat(foundClient).isNotNull();
        assertThat(foundClient.getUid()).isEqualTo(testClientUid);
        assertThat(foundClient.getEmail()).isEqualTo(testEmail);
        assertThat(foundClient.getUsername()).isEqualTo(testUsername);

        log.info("✅ Successfully added and retrieved client: {}", testClientUid);
    }

    @Test
    @DisplayName("Should throw exception when adding client with duplicate email")
    void shouldThrowExceptionWhenAddingClientWithDuplicateEmail() {
        // Given
        ClientBlm firstClient = ClientBlm.builder()
                .uid(testClientUid)
                .email(testEmail)
                .username(testUsername)
                .password(testPassword)
                .birthDate(new Date())
                .build();

        clientRepository.add(firstClient);

        // When & Then
        ClientBlm duplicateEmailClient = ClientBlm.builder()
                .uid(UUID.randomUUID())
                .email(testEmail) // тот же email
                .username("different_username")
                .password("DifferentPassword123")
                .birthDate(new Date())
                .build();

        assertThatThrownBy(() -> clientRepository.add(duplicateEmailClient))
                .isInstanceOf(ClientAlreadyExisistsException.class);

        log.info("✅ Correctly prevented duplicate email");
    }

    @Test
    @DisplayName("Should find client by email")
    void shouldFindClientByEmail() {
        // Given
        createTestClientInDatabase();

        // When
        ClientBlm foundClient = clientRepository.findByEmail(testEmail);

        // Then
        assertThat(foundClient).isNotNull();
        assertThat(foundClient.getEmail()).isEqualTo(testEmail);
        assertThat(foundClient.getUid()).isEqualTo(testClientUid);

        log.info("✅ Successfully found client by email: {}", testEmail);
    }

    @Test
    @DisplayName("Should find client by username")
    void shouldFindClientByUsername() {
        // Given
        createTestClientInDatabase();

        // When
        ClientBlm foundClient = clientRepository.findByUsername(testUsername);

        // Then
        assertThat(foundClient).isNotNull();
        assertThat(foundClient.getUsername()).isEqualTo(testUsername);
        assertThat(foundClient.getUid()).isEqualTo(testClientUid);

        log.info("✅ Successfully found client by username: {}", testUsername);
    }

    @Test
    @DisplayName("Should find client by email and password")
    void shouldFindClientByEmailAndPassword() {
        // Given
        createTestClientInDatabase();

        // When
        ClientBlm foundClient = clientRepository.findByEmailPassword(testEmail, testPassword);

        // Then
        assertThat(foundClient).isNotNull();
        assertThat(foundClient.getEmail()).isEqualTo(testEmail);
        assertThat(foundClient.getUid()).isEqualTo(testClientUid);

        log.info("✅ Successfully authenticated client by email and password");
    }

    @Test
    @DisplayName("Should find client by username and password")
    void shouldFindClientByUsernameAndPassword() {
        // Given
        createTestClientInDatabase();

        // When
        ClientBlm foundClient = clientRepository.findByUsernamePassword(testUsername, testPassword);

        // Then
        assertThat(foundClient).isNotNull();
        assertThat(foundClient.getUsername()).isEqualTo(testUsername);
        assertThat(foundClient.getUid()).isEqualTo(testClientUid);

        log.info("✅ Successfully authenticated client by username and password");
    }

    @Test
    @DisplayName("Should throw exception when client not found by UID")
    void shouldThrowExceptionWhenClientNotFoundByUid() {
        // Given
        UUID nonExistentUid = UUID.randomUUID();

        // When & Then
        assertThatThrownBy(() -> clientRepository.findByUid(nonExistentUid))
                .isInstanceOf(ClientNotFoundException.class);

        log.info("✅ Correctly handled non-existent client UID");
    }

    @Test
    @DisplayName("Should throw exception when authentication fails with wrong password")
    void shouldThrowExceptionWhenAuthenticationFailsWithWrongPassword() {
        // Given
        createTestClientInDatabase();

        // When & Then
        assertThatThrownBy(() -> clientRepository.findByEmailPassword(testEmail, "wrongpassword"))
                .isInstanceOf(ClientNotFoundException.class);

        log.info("✅ Correctly failed authentication with wrong password");
    }

    @Test
    @DisplayName("Should delete client successfully")
    void shouldDeleteClientSuccessfully() {
        // Given
        createTestClientInDatabase();

        // Verify client exists
        ClientBlm existingClient = clientRepository.findByUid(testClientUid);
        assertThat(existingClient).isNotNull();

        // When
        clientRepository.deleteByUid(testClientUid);

        // Then - клиент не должен больше существовать
        assertThatThrownBy(() -> clientRepository.findByUid(testClientUid))
                .isInstanceOf(ClientNotFoundException.class);

        log.info("✅ Successfully deleted client: {}", testClientUid);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent client")
    void shouldThrowExceptionWhenDeletingNonExistentClient() {
        // Given
        UUID nonExistentUid = UUID.randomUUID();

        // When & Then
        assertThatThrownBy(() -> clientRepository.deleteByUid(nonExistentUid))
                .isInstanceOf(ClientNotFoundException.class);

        log.info("✅ Correctly prevented deletion of non-existent client");
    }

}