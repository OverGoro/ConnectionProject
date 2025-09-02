package com.service.auth;

import com.service.auth.model.AccessTokenBLM;
import com.service.auth.model.ClientBLM;
import com.service.auth.model.RefreshTokenBLM;

import ch.qos.logback.core.joran.sanity.Pair;


public interface AuthService {
    public Pair<AccessTokenBLM, RefreshTokenBLM> authorize(ClientBLM clientBLM);
    public void register(ClientBLM clientBLM);
    public Pair<AccessTokenBLM, RefreshTokenBLM> refresh(RefreshTokenBLM refreshTokenBLM);
}
