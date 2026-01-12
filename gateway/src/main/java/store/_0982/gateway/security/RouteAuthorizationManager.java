package store._0982.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import store._0982.gateway.domain.GatewayRouteRepository;
import store._0982.gateway.security.token.MemberAuthenticationToken;


@Component
@RequiredArgsConstructor
public class RouteAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    private final GatewayRouteRepository gatewayRouteRepository;

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {
        ServerWebExchange exchange = context.getExchange();
        return authentication
                .ofType(MemberAuthenticationToken.class)
                .flatMap(memberAuthenticationToken -> {
                    String memberRole = memberAuthenticationToken.getRole().name();
                    String method = exchange.getRequest().getMethod().name();
                    String path = exchange.getRequest().getURI().getPath();
                    return gatewayRouteRepository
                            .findByMethodAndEndpoint(method, path)
                            .map(gatewayRoute -> {
                                if (gatewayRoute == null || gatewayRoute.getRoles() == null) {
                                    return new AuthorizationDecision(false);
                                }
                                boolean allowed = gatewayRoute.getRoles().contains(memberRole);
                                return new AuthorizationDecision(allowed);
                            })
                            .defaultIfEmpty(new AuthorizationDecision(false));
                });
    }
}
