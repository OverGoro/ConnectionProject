package com.service.auth;

import org.springframework.stereotype.Service;

import com.service.auth.config.ApplicationConfig;
import com.service.auth.config.JwtConfig;
import com.service.auth.converter.AccessTokenConverterImpl;
import com.service.auth.converter.ClientConverterImpl;
import com.service.auth.converter.RefreshTokenConverterImpl;
import com.service.auth.model.AccessTokenBLM;
import com.service.auth.model.AccessTokenDALM;
import com.service.auth.model.AccessTokenDTO;
import com.service.auth.model.ClientBLM;
import com.service.auth.model.ClientDTO;
import com.service.auth.model.RefreshTokenDTO;
import com.service.auth.repository.token.access.AccessTokenRepository;

import ch.qos.logback.core.joran.sanity.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService{
    private final AccessTokenConverterImpl accessTokenConverter;
    private final RefreshTokenConverterImpl refreshTokenConverter;
    private final ClientConverterImpl clientConverter;

    private final AccessTokenRepository accessTokenRepository;

    @Override
    public Pair<AccessTokenDTO, RefreshTokenDTO> authorize(ClientDTO clientDTO) {
        ClientBLM CLientBLM = clientConverter.toBLM(clientDTO);
        AccessTokenDALM accessTokenDALM = accessTokenRepository.create(CLientBLM);
        AccessTokenBLM accessTokenBLM = accessTokenConverter.toBLM(accessTokenDALM);
        
        log.info("Created token: " + accessTokenBLM.getToken().toString());

        throw new UnsupportedOperationException("Unimplemented method 'register'");
    }

    @Override
    public void register(ClientDTO clientDTO) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'register'");
    }

    @Override
    public Pair<AccessTokenDTO, RefreshTokenDTO> refresh(RefreshTokenDTO refreshTokenDTO) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'refresh'");
    }

}
