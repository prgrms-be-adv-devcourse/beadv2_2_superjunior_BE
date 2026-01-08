package store._0982.gateway.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import store._0982.gateway.domain.GatewayRoute;
import store._0982.gateway.domain.GatewayRouteRepository;


@Repository
@RequiredArgsConstructor
public class GatewayRouteR2dbcRepository implements GatewayRouteRepository {

    private final GatewayRouteReactiveCrudRepository gatewayRouteReactiveCrudRepository;

    @Override
    public Mono<GatewayRoute> findByMethodAndEndpoint(String httpMethod, String endpoint) {
        return gatewayRouteReactiveCrudRepository.findByMethodAndEndpoint(httpMethod, endpoint);
    }
}
