// DeviceAuthServiceImpl.java
package com.service.device.auth;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.connection.device.model.DeviceDALM;
import com.connection.device.repository.DeviceRepository;
import com.connection.device.token.converter.DeviceAccessTokenConverter;
import com.connection.device.token.converter.DeviceTokenConverter;
import com.connection.device.token.exception.DeviceAccessTokenExistsException;
import com.connection.device.token.exception.DeviceAccessTokenNotFoundException;
import com.connection.device.token.exception.DeviceTokenNotFoundException;
import com.connection.device.token.generator.DeviceAccessTokenGenerator;
import com.connection.device.token.generator.DeviceTokenGenerator;
import com.connection.device.token.model.DeviceAccessTokenBLM;
import com.connection.device.token.model.DeviceAccessTokenDALM;
import com.connection.device.token.model.DeviceTokenBLM;
import com.connection.device.token.model.DeviceTokenDALM;
import com.connection.device.token.repository.DeviceAccessTokenRepository;
import com.connection.device.token.repository.DeviceTokenRepository;
import com.connection.device.token.validator.DeviceAccessTokenValidator;
import com.connection.device.token.validator.DeviceTokenValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@EnableAutoConfiguration(exclude = {
    JpaRepositoriesAutoConfiguration.class
})
@Transactional("atomicosTransactionManager")
public class DeviceAuthServiceImpl implements DeviceAuthService {
    
    private final DeviceRepository deviceRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final DeviceAccessTokenRepository deviceAccessTokenRepository;

    private final DeviceTokenConverter deviceTokenConverter;
    private final DeviceAccessTokenConverter deviceAccessTokenConverter;

    private final DeviceTokenValidator deviceTokenValidator;
    private final DeviceAccessTokenValidator deviceAccessTokenValidator;

    private final DeviceTokenGenerator deviceTokenGenerator;
    private final DeviceAccessTokenGenerator deviceAccessTokenGenerator;
    
    @Qualifier("jwtAccessTokenExpiration")
    private final Duration jwtAccessTokenDuration;

    @Qualifier("jwtRefreshTokenExpiration")
    private final Duration jwtRefreshTokenDuration;


    @Override
    public DeviceAccessTokenBLM authorizeByToken(DeviceTokenBLM deviceToken) {
        log.info("Authorizing device with token");
        
        deviceTokenValidator.validate(deviceToken);
        
        DeviceDALM device = deviceRepository.findByUid(deviceToken.getDeviceUid());
        log.info("Device found: {}", device.getUid());

        Date createdAt = new Date();
        Date expiresAt = Date.from(createdAt.toInstant().plus(jwtRefreshTokenDuration));
        
        String deviceAccessTokenString = deviceAccessTokenGenerator.generateDeviceAccessToken(deviceToken.getUid(), createdAt , expiresAt);
        DeviceAccessTokenBLM deviceAccessTokenBLM = deviceAccessTokenGenerator.getDeviceAccessTokenBLM(deviceAccessTokenString);
        DeviceAccessTokenDALM deviceAccessTokenDALM = deviceAccessTokenConverter.toDALM(deviceAccessTokenBLM);

        DeviceAccessTokenDALM oldDeviceAccessTokenDALM;
        try{
            oldDeviceAccessTokenDALM = deviceAccessTokenRepository.findByDeviceTokenUid(deviceToken.getUid());
        } 
        catch (DeviceTokenNotFoundException e){
            oldDeviceAccessTokenDALM = null;
        }

        if (oldDeviceAccessTokenDALM != null && oldDeviceAccessTokenDALM.getExpiresAt().after(new Date())){
            throw new DeviceAccessTokenExistsException("Cannot authorize, there are some access tokens created. New token will be available after their expiration.");
        }
        
        deviceAccessTokenRepository.add(deviceAccessTokenDALM);
        
        log.info("Device authorized successfully. Device UID: {}", deviceToken.getDeviceUid());
        return deviceAccessTokenBLM;
    }

    @Override
    public void validateDeviceAccessToken(DeviceAccessTokenBLM deviceAccessTokenBLM) {
        log.debug("Validating device access token");
        deviceAccessTokenValidator.validate(deviceAccessTokenBLM);
    }

    @Override
    public void validateDeviceToken(DeviceTokenBLM deviceTokenBLM) {
        log.debug("Validating device token");
        deviceTokenValidator.validate(deviceTokenBLM);        
    }

    @Override
    public UUID getDeviceUid(DeviceAccessTokenBLM accessTokenBLM) {
        validateDeviceAccessToken(accessTokenBLM);
        
        try {
            DeviceAccessTokenDALM deviceAccessTokenDALM = deviceAccessTokenRepository.findByToken(accessTokenBLM.getToken());
            DeviceTokenDALM deviceTokenDALM = deviceTokenRepository.findByUid(deviceAccessTokenDALM.getDeviceTokenUid());
            return deviceTokenDALM.getDeviceUid();
        } catch (DeviceAccessTokenNotFoundException | DeviceTokenNotFoundException e) {
            throw new SecurityException("Failed to extract device UID from token");
        }
    }

    @Override
    public UUID getDeviceUid(DeviceTokenBLM deviceTokenBLM) {
        validateDeviceToken(deviceTokenBLM);
        
        try {
            DeviceTokenDALM deviceTokenDALM = deviceTokenRepository.findByToken(deviceTokenBLM.getToken());
            return deviceTokenDALM.getDeviceUid();
        } catch (DeviceTokenNotFoundException e) {
            throw new SecurityException("Failed to extract device UID from token");
        }
    }
}