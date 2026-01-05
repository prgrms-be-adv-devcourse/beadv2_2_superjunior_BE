package store._0982.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import store._0982.gateway.infrastructure.jwt.GatewayJwtProvider;

import java.util.Collections;
import java.util.UUID;

/**
 * accessToken 쿠키에서 JWT 를 읽어 Authentication 으로 변환하는 컨버터.
 * - 쿠키가 없으면 인증 시도를 하지 않고 빈 Mono 를 반환
 */
@RequiredArgsConstructor
public class CookieServerAuthenticationConverter implements ServerAuthenticationConverter {

    private final GatewayJwtProvider jwtProvider;

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        HttpCookie accessTokenCookie = exchange.getRequest()
                .getCookies()
                .getFirst("accessToken");
        // 쿠키가 없거나 비어 있으면 인증 시도하지 않음 (익명으로 처리)
        if (accessTokenCookie == null || accessTokenCookie.getValue().isBlank()) {
            return Mono.just(new MemberAuthenticationToken());
        }

        String token = accessTokenCookie.getValue();

        try {
            Claims claims = jwtProvider.parseToken(token);
            UUID memberId = UUID.fromString(claims.getSubject());

            // 이후 인가 단계에서 재사용할 수 있도록 exchange attribute 에 저장
            exchange.getAttributes().put("memberId", memberId);

            Authentication authentication = new MemberAuthenticationToken(memberId);
            return Mono.just(authentication);

        } catch (JwtException | IllegalArgumentException e) {
            return Mono.error(e);
        }
    }

    private static class MemberAuthenticationToken extends AbstractAuthenticationToken {

        private final UUID memberId;

        MemberAuthenticationToken(UUID memberId) {
            super(Collections.emptyList());
            this.memberId = memberId;
            setAuthenticated(true);
        }

        MemberAuthenticationToken() {
            super(Collections.emptyList());
            //authentication.map(Authentication::getPrinciple)이 null이면 에러가 남
            this.memberId = UUID.randomUUID();
            setAuthenticated(true);
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return memberId;
        }
    }
}
