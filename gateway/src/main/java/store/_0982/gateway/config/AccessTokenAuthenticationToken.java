package store._0982.gateway.config;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/**
 * 인증 쿠키에서 추출한 access token을 담는 미인증 토큰.
 */
public class AccessTokenAuthenticationToken extends AbstractAuthenticationToken {

    private final String token;

    public AccessTokenAuthenticationToken(String token) {
        super(Collections.emptyList());
        this.token = token;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }
}
