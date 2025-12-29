package store._0982.gateway.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import store._0982.gateway.domain.GatewayRouteRepository;
import store._0982.gateway.domain.Member;
import store._0982.gateway.domain.MemberRepository;

import java.util.UUID;


@Component
@RequiredArgsConstructor
public class GlobalAuthorizationFilter implements GlobalFilter, Ordered { //권한 인가

    private GatewayRouteRepository gatewayRouteRepository;
    private MemberRepository memberRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        UUID memberId = (UUID)(exchange.getAttribute("memberId"));
        Member member = memberRepository.findById(memberId).orElse(null);


        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
