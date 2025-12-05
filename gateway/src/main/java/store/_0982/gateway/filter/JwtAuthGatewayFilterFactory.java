package store._0982.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import store._0982.gateway.domain.AuthMember;
import store._0982.gateway.infrastructure.jwt.GatewayJwtProvider;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 특정 라우트에서만 적용할 수 있는 JWT 인증 필터.
 * application.yml 의 filters 섹션에서 "JwtAuth" 로 참조할 수 있다.
 */
@Component
public class JwtAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthGatewayFilterFactory.Config> { // GatewayFilterFactory 떼면 매핑 안됨.

    private final GatewayJwtProvider jwtProvider;

    public JwtAuthGatewayFilterFactory(GatewayJwtProvider jwtProvider) {
        super(Config.class);
        this.jwtProvider = jwtProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            HttpCookie accessTokenCookie = exchange.getRequest()
                    .getCookies()
                    .getFirst("accessToken");

            // 쿠키에 토큰이 아예 없는 경우: 헤더 추가 없이 그대로 통과
            if (accessTokenCookie == null || accessTokenCookie.getValue() == null
                    || accessTokenCookie.getValue().isBlank()) {
                return chain.filter(exchange);
            }

            String token = accessTokenCookie.getValue();

            AuthMember authMember;
            try {
                Claims claims = jwtProvider.parseToken(token);

                Date expiration = claims.getExpiration();
                if (expiration != null && expiration.before(new Date())) {  // 토큰이 있으나, 만료된 경우 돌려보냄.
                    return unauthorized(exchange, "Token expired");
                }

                String memberId = claims.getSubject();
                String email = claims.get("email", String.class);
                String role = claims.get("role", String.class);
                authMember = new AuthMember(memberId, email, role);
            } catch (JwtException | IllegalArgumentException e) {
                return unauthorized(exchange, "Invalid token");
            }

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .headers(headers -> {
                        // 1) 먼저 클라이언트가 보낸 값 제거
                        headers.remove("X-Member-Id");
                        headers.remove("X-Member-Email");
                        headers.remove("X-Member-Role");

                        // 2) Gateway가 검증한 정보로 덮어쓰기
                        headers.set("X-Member-Id", authMember.getId());
                        headers.set("X-Member-Email", authMember.getEmail());
                        headers.set("X-Member-Role", authMember.getRole());
                    })
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        /**
         * 필터 동작 설정을 위한 Config.
         * 현재는 비어있지만, 필요 시 requiredRole 등의 옵션을 추가할 수 있다.
         */
    }
}
