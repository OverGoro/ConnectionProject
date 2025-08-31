package com.service.auth;

import com.service.auth.model.AccessTokenDTO;
import com.service.auth.model.ClientDTO;
import com.service.auth.model.RefreshTokenDTO;

import ch.qos.logback.core.joran.sanity.Pair;


public interface AuthService {
    public Pair<AccessTokenDTO, RefreshTokenDTO> authorize(ClientDTO clientDTO);
    public void register(ClientDTO clientDTO);
    public Pair<AccessTokenDTO, RefreshTokenDTO> refresh(RefreshTokenDTO refreshTokenDTO);
}
