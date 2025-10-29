package com.connection.service.auth;

import java.util.Map;

import org.springframework.data.util.Pair;

import com.connection.client.model.ClientBLM;
import com.connection.token.model.AccessTokenBLM;
import com.connection.token.model.RefreshTokenBLM;



public interface AuthService {
    public Pair<AccessTokenBLM, RefreshTokenBLM> authorizeByEmail(String email, String password);
    public void register(ClientBLM clientBLM);
    public Pair<AccessTokenBLM, RefreshTokenBLM> refresh(RefreshTokenBLM refreshTokenBLM);
    public void validateAccessToken(AccessTokenBLM accessTokenBLM);
    public void validateRefreshToken(RefreshTokenBLM refreshTokenBLM);

    public AccessTokenBLM validateAccessToken(String token);

    public Map<String, Object> getHealthStatus();
}
