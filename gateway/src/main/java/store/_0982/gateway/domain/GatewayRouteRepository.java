package store._0982.gateway.domain;

import reactor.core.publisher.Mono;

public interface GatewayRouteRepository {
    Mono<GatewayRoute> findByMethodAndEndpoint(String httpMethod, String endpoint);
}
