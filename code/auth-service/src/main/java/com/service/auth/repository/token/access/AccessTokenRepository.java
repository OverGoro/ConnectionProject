package com.service.auth.repository.token.access;

import com.service.auth.model.AccessTokenDALM;
import com.service.auth.model.ClientBLM;

public interface AccessTokenRepository {
    public AccessTokenDALM create(ClientBLM clientBLM);
}
