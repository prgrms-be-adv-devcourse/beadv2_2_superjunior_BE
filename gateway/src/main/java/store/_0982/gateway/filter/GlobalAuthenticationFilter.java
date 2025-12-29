package store._0982.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import store._0982.gateway.exception.CustomErrorCode;
import store._0982.gateway.exception.ExceptionHandler;
import store._0982.gateway.infrastructure.jwt.GatewayJwtProvider;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GlobalAuthenticationFilter implements GlobalFilter, Ordered { // 토큰 인증

    private final GatewayJwtProvider jwtProvider;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpCookie accessTokenCookie = exchange.getRequest()
                .getCookies()
                .getFirst("accessToken");

        // 쿠키에 토큰이 아예 없는 경우: 헤더 추가 없이 그대로 통과
        if (accessTokenCookie == null || accessTokenCookie.getValue().isBlank()) {
            return chain.filter(exchange);
        }
        String token = accessTokenCookie.getValue();
        UUID memberId = null;
        try {
            Claims claims = jwtProvider.parseToken(token); //여기서 예외 던짐.
            memberId = UUID.fromString(claims.getSubject());
        } catch (ExpiredJwtException e) {
            return ExceptionHandler.responseException(exchange, CustomErrorCode.EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            return ExceptionHandler.responseException(exchange, CustomErrorCode.INVALID);
        }

        exchange.getAttributes().put("memberId", memberId);

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
