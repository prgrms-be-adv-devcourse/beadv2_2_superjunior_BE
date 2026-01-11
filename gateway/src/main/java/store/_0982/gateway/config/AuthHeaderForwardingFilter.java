package store._0982.gateway.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 인증된 사용자 정보를 다운스트림 서비스로 전달하기 위해 헤더를 추가하는 필터.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthHeaderForwardingFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .flatMap(auth -> {
                    if (auth instanceof MemberAuthenticationToken token && token.getPrincipal() != null) {
                        UUID memberId = (UUID) token.getPrincipal();
                        var mutatedRequest = exchange.getRequest().mutate()
                                .header("X-Member-Id", memberId.toString())
                                .build();
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }
}
