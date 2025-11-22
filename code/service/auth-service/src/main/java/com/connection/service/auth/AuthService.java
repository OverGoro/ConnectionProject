package com.connection.service.auth;

import com.connection.client.model.ClientBlm;
import com.connection.token.model.AccessTokenBlm;
import com.connection.token.model.RefreshTokenBlm;
import java.util.Map;
import org.springframework.data.util.Pair;

/** . */
public interface AuthService {
    /** . */
    public Pair<AccessTokenBlm, RefreshTokenBlm> authorizeByEmail(String email,
            String password);

    /** . */
    public void register(ClientBlm clientBlm);

    /** . */
    public Pair<AccessTokenBlm, RefreshTokenBlm> refresh(
            RefreshTokenBlm refreshTokenBlm);

    /** . */
    public void validateAccessToken(AccessTokenBlm accessTokenBlm);
    
    /** . */
    public AccessTokenBlm validateAccessToken(String token);

    /** . */
    public void validateRefreshToken(RefreshTokenBlm refreshTokenBlm);

    /** . */
    public Map<String, Object> getHealthStatus();
}
