package store._0982.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import store._0982.gateway.domain.AuthMember;
import store._0982.gateway.domain.Role;
import store._0982.gateway.exception.CustomErrorCode;
import store._0982.gateway.exception.ExceptionHandler;
import store._0982.gateway.infrastructure.jwt.GatewayJwtProvider;

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
                Claims claims = jwtProvider.parseToken(token); //여기서 예외 던짐.
                String memberId = claims.getSubject();
                String email = claims.get("email", String.class);
                String role = claims.get("role", String.class);
                authMember = new AuthMember(memberId, email, Role.valueOf(role));
            } catch (ExpiredJwtException e) {
                return ExceptionHandler.responseException(exchange, CustomErrorCode.EXPIRED);
            } catch (JwtException | IllegalArgumentException e) {
                return ExceptionHandler.responseException(exchange, CustomErrorCode.INVALID);
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
                        headers.set("X-Member-Role", authMember.getRole().name());
                    })
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }

    public static class Config {
    }
}
