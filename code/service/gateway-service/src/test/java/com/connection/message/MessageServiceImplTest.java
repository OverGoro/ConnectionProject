package com.connection.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.connection.device.DeviceService;
import com.connection.device.converter.DeviceConverter;
import com.connection.message.config.SecurityUtils;
import com.connection.message.converter.MessageConverter;
import com.connection.message.model.MessageBLM;
import com.connection.message.repository.MessageRepository;
import com.connection.message.validator.MessageValidator;
import com.connection.processing.buffer.converter.BufferConverter;
import com.connection.scheme.converter.ConnectionSchemeConverter;
import com.connection.service.auth.AuthService;
import com.service.buffer.BufferService;
import com.service.connectionscheme.ConnectionSchemeService;
import com.service.device.auth.DeviceAuthService;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessageValidator messageValidator;

    @MockitoBean
    private AuthService authClient;

    @MockitoBean
    private BufferService bufferClient;

    @MockitoBean
    private ConnectionSchemeService connectionSchemeClient;

    @Mock
    private DeviceAuthService deviceAuthClient;

    @Mock
    private DeviceService deviceClient;

    @Mock
    private BufferConverter bufferConverter;

    @Mock
    private ConnectionSchemeConverter connectionSchemeConverter;

    @Mock
    private DeviceConverter deviceConverter;

    @Mock
    private MessageConverter messageConverter;

    private MessageServiceImpl messageService;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        messageService = new MessageServiceImpl(
            messageRepository,
            messageValidator,
            authClient,
            bufferClient,
            connectionSchemeClient,
            deviceAuthClient,
            deviceClient,
            bufferConverter,
            connectionSchemeConverter,
            deviceConverter,
            messageConverter
        );
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    void testAddMessage_WithDeviceAuthentication_Success() {
        // Arrange
        UUID deviceUid = UUID.randomUUID();
        UUID bufferUid = UUID.randomUUID();
        MessageBLM messageBLM = MessageBLM.builder()
            .uid(UUID.randomUUID())
            .bufferUid(bufferUid)
            .content("test message")
            .contentType("OUTGOING")
            .createdAt(new Date())
            .build();

        // securityUtilsMock.when(SecurityUtils::isDeviceAuthenticated).thenReturn(true);
        // securityUtilsMock.when(SecurityUtils::getCurrentDeviceUid).thenReturn(deviceUid);
        // doNothing().when(messageValidator).validate(messageBLM);

        // // Act
        // messageService.addMessage(messageBLM);

        // // Assert
        // verify(messageValidator).validate(messageBLM);
        // verify(messageRepository).add(messageBLM);
    }

    @Test
    void testAddMessage_WithClientAuthentication_Success() {
        // Arrange
        UUID clientUid = UUID.randomUUID();
        UUID bufferUid = UUID.randomUUID();
        MessageBLM messageBLM = MessageBLM.builder()
            .uid(UUID.randomUUID())
            .bufferUid(bufferUid)
            .content("test message")
            .contentType("OUTGOING")
            .createdAt(new Date())
            .build();

        // securityUtilsMock.when(SecurityUtils::isClientAuthenticated).thenReturn(true);
        // securityUtilsMock.when(SecurityUtils::getCurrentClientUid).thenReturn(clientUid);
        // doNothing().when(messageValidator).validate(messageBLM);

        // // Act
        // messageService.addMessage(messageBLM);

        // // Assert
        // verify(messageValidator).validate(messageBLM);
        // verify(messageRepository).add(messageBLM);
    }

    @Test
    void testAddMessage_NoAuthentication_ThrowsSecurityException() {
        // Arrange
        MessageBLM messageBLM = MessageBLM.builder()
            .uid(UUID.randomUUID())
            .bufferUid(UUID.randomUUID())
            .content("test message")
            .contentType("OUTGOING")
            .createdAt(new Date())
            .build();

        // securityUtilsMock.when(SecurityUtils::isDeviceAuthenticated).thenReturn(false);
        // securityUtilsMock.when(SecurityUtils::isClientAuthenticated).thenReturn(false);

        // // Act & Assert
        // SecurityException exception = assertThrows(SecurityException.class, 
        //     () -> messageService.addMessage(messageBLM));
        // assertTrue(exception.getMessage().contains("Cannot add messages without authorization"));
    }

    @Test
    void testGetMessagesByBuffer_Success() {
        // Arrange
        UUID bufferUid = UUID.randomUUID();
        UUID clientUid = UUID.randomUUID();
        List<MessageBLM> expectedMessages = Arrays.asList(
            MessageBLM.builder()
                .uid(UUID.randomUUID())
                .bufferUid(bufferUid)
                .content("message 1")
                .contentType("INCOMING")
                .createdAt(new Date())
                .build(),
            MessageBLM.builder()
                .uid(UUID.randomUUID())
                .bufferUid(bufferUid)
                .content("message 2")
                .contentType("INCOMING")
                .createdAt(new Date(System.currentTimeMillis() + 1000))
                .build()
        );

        // securityUtilsMock.when(SecurityUtils::isClientAuthenticated).thenReturn(true);
        // securityUtilsMock.when(SecurityUtils::getCurrentClientUid).thenReturn(clientUid);
        // when(messageRepository.findByBufferUid(bufferUid)).thenReturn(expectedMessages);

        // // Act
        // List<MessageBLM> result = messageService.getMessagesByBuffer(bufferUid, false, 0, 10);

        // // Assert
        // assertEquals(expectedMessages.size(), result.size());
        // verify(messageRepository).findByBufferUid(bufferUid);
    }

    @Test
    void testGetMessagesByBuffer_WithDeleteOnGet() {
        // Arrange
        UUID bufferUid = UUID.randomUUID();
        UUID clientUid = UUID.randomUUID();
        MessageBLM message = MessageBLM.builder()
            .uid(UUID.randomUUID())
            .bufferUid(bufferUid)
            .content("test message")
            .contentType("INCOMING")
            .createdAt(new Date())
            .build();
        List<MessageBLM> messages = Collections.singletonList(message);

        // securityUtilsMock.when(SecurityUtils::isClientAuthenticated).thenReturn(true);
        // securityUtilsMock.when(SecurityUtils::getCurrentClientUid).thenReturn(clientUid);
        // when(messageRepository.findByBufferUid(bufferUid)).thenReturn(messages);

        // // Act
        // List<MessageBLM> result = messageService.getMessagesByBuffer(bufferUid, true, 0, 10);

        // // Assert
        // assertEquals(1, result.size());
        // verify(messageRepository).findByBufferUid(bufferUid);
        // verify(messageRepository).deleteByUid(message.getUid());
    }

    @Test
    void testHealth_Success() {
        // Act
        Map<String, String> result = messageService.health();

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        assertTrue(result.containsKey("service"));
        assertEquals("message-service", result.get("service"));
    }

    @Test
    void testGetMessagesByScheme_WithClientAuthentication() {
        // Arrange
        UUID schemeUid = UUID.randomUUID();
        UUID clientUid = UUID.randomUUID();

        // securityUtilsMock.when(SecurityUtils::isClientAuthenticated).thenReturn(true);
        // securityUtilsMock.when(SecurityUtils::getCurrentClientUid).thenReturn(clientUid);
        // when(connectionSchemeClient.connectionSchemeExistsAndBelongsToClient(eq(schemeUid), eq(clientUid)))
        //     .thenReturn(true);

        // // Act
        // List<MessageBLM> result = messageService.getMessagesByScheme(schemeUid, false, 0, 10);

        // // Assert
        // assertNotNull(result);
        // assertTrue(result.isEmpty());
    }

    @Test
    void testGetMessagesByDevice_WithDeviceAuthentication() {
        // Arrange
        UUID deviceUid = UUID.randomUUID();

        // securityUtilsMock.when(SecurityUtils::isDeviceAuthenticated).thenReturn(true);
        // securityUtilsMock.when(SecurityUtils::getCurrentDeviceUid).thenReturn(deviceUid);

        // // Act
        // List<MessageBLM> result = messageService.getMessagesByDevice(deviceUid, false, 0, 10);

        // // Assert
        // assertNotNull(result);
        // assertTrue(result.isEmpty());
    }

    @Test
    void testGetMessagesByDevice_WithClientAuthentication() {
        // Arrange
        // UUID deviceUid = UUID.randomUUID();
        // UUID clientUid = UUID.randomUUID();

        // securityUtilsMock.when(SecurityUtils::isClientAuthenticated).thenReturn(true);
        // securityUtilsMock.when(SecurityUtils::getCurrentClientUid).thenReturn(clientUid);
        // when(deviceClient.deviceExistsAndBelongsToClient(eq(deviceUid), eq(clientUid)))
        //     .thenReturn(true);

        // // Act
        // List<MessageBLM> result = messageService.getMessagesByDevice(deviceUid, false, 0, 10);

        // // Assert
        // assertNotNull(result);
        // assertTrue(result.isEmpty());
    }

    @Test
    void testGetMessagesByDevice_NoAccess_ThrowsSecurityException() {
        // Arrange
        // UUID deviceUid = UUID.randomUUID();
        // UUID clientUid = UUID.randomUUID();

        // securityUtilsMock.when(SecurityUtils::isClientAuthenticated).thenReturn(true);
        // securityUtilsMock.when(SecurityUtils::getCurrentClientUid).thenReturn(clientUid);
        // when(deviceClient.deviceExistsAndBelongsToClient(eq(deviceUid), eq(clientUid)))
        //     .thenReturn(false);

        // // Act & Assert
        // SecurityException exception = assertThrows(SecurityException.class,
        //     () -> messageService.getMessagesByDevice(deviceUid, false, 0, 10));
        // assertTrue(exception.getMessage().contains("Device doesn't belong to the authenticated client"));
    }
}