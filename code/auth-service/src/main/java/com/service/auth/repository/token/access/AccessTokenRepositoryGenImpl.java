package com.service.auth.repository.token.access;

import java.time.Duration;
import java.util.Date;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.service.auth.model.AccessTokenDALM;
import com.service.auth.model.ClientBLM;

@Repository
public class AccessTokenRepositoryGenImpl implements AccessTokenRepository {

    @Qualifier("jwtAccessTokenExpiration")
    Duration jwtAccessTokenDuration;

    @Qualifier("jwtRefreshTokenExpiration")
    Duration jwtRefreshTokenDuration;

    @Override
    public AccessTokenDALM create(ClientBLM clientBLM) {
        Date currentDate = new Date();
        return new AccessTokenDALM(clientBLM.getUid(), currentDate,
                Date.from(currentDate.toInstant().plus(jwtAccessTokenDuration)));
    }

}
