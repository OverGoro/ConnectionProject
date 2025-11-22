
package com.service.device.auth.config;

import com.connection.device.token.converter.DeviceTokenConverter;
import com.connection.device.token.generator.DeviceTokenGenerator;
import com.connection.device.token.validator.DeviceTokenValidator;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/** . */
@Configuration
@Primary
public class DeviceTokenUtilsConfig {

    @Bean("deviceTokenValidator")
    DeviceTokenValidator deviceTokenValidator() {
        return new DeviceTokenValidator();
    }

    @Bean("deviceTokenGenerator")
    DeviceTokenGenerator deviceTokenGenerator(SecretKey jwtSecretKey,
            @Qualifier("appName") String appNameString,
            @Qualifier("deviceJwtSubject") String subjectString) {
        return new DeviceTokenGenerator(jwtSecretKey, appNameString,
                subjectString);
    }

    @Bean("deviceTokenConverter")
    DeviceTokenConverter deviceTokenConverter(
            DeviceTokenGenerator deviceTokenGenerator) {
        return new DeviceTokenConverter(deviceTokenGenerator);
    }
}
