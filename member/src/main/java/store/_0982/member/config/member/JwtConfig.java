package store._0982.member.config.member;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import store._0982.member.infrastructure.member.JwtProvider;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-validity-period}")
    private long accessTokenValidityPeriod; //ms 단위

    @Value("${jwt.refresh-token-validity-period}")
    private long refreshTokenValidityPeriod; //ms 단위

    @Bean
    public JwtProvider jwtProvider() {
        return new JwtProvider(jwtSecret, accessTokenValidityPeriod, refreshTokenValidityPeriod);
    }
}
