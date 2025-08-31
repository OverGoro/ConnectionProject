package com.service.auth.repository.token.access;

import java.time.Duration;
import java.util.Date;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.service.auth.model.AccessTokenDALM;
import com.service.auth.model.ClientBLM;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AccessTokenRepositoryGenImpl implements AccessTokenRepository {

    @Qualifier("jwtAccessTokenExpiration")
    private final Duration jwtAccessTokenDuration;

    @Qualifier("jwtRefreshTokenExpiration")
    private final Duration jwtRefreshTokenDuration;

    @Override
    public AccessTokenDALM create(ClientBLM clientBLM) {
        Date currentDate = new Date();
        return new AccessTokenDALM(clientBLM.getUid(), currentDate,
                Date.from(currentDate.toInstant().plus(jwtAccessTokenDuration)));
    }

}
