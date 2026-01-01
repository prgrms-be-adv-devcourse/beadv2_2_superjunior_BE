package store._0982.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import store._0982.gateway.domain.GatewayRouteRepository;
import store._0982.gateway.domain.Member;
import store._0982.gateway.domain.MemberCache;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RouteAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    private final GatewayRouteRepository gatewayRouteRepository;
    private final MemberCache memberCache;

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {
        ServerWebExchange exchange = context.getExchange();
        return authentication
                .map(Authentication::getPrincipal)
                .ofType(UUID.class)
                .flatMap(memberId -> Mono.justOrEmpty(memberCache.findById(memberId)))
                .defaultIfEmpty(Member.createGuest())
                .flatMap(member -> {
                    String method = exchange.getRequest().getMethod().name();
                    String path = exchange.getRequest().getURI().getPath();

                    return gatewayRouteRepository
                            .findByMethodAndEndpoint(method, path)
                            .map(gatewayRoute -> {
                                if (gatewayRoute == null || gatewayRoute.getRoles() == null) {
                                    return new AuthorizationDecision(false);
                                }

                                boolean allowed = gatewayRoute.getRoles().contains(member.getRole().name());
                                return new AuthorizationDecision(allowed);
                            })
                            .defaultIfEmpty(new AuthorizationDecision(false));
                });
    }
}
