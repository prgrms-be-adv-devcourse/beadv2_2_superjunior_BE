package store._0982.gateway.infrastructure;

import org.springframework.stereotype.Repository;
import store._0982.gateway.domain.GatewayRoute;
import store._0982.gateway.domain.GatewayRouteRepository;

import java.util.Optional;

@Repository
public class GatewayRouteR2dbcRepository implements GatewayRouteRepository {

    @Override
    public Optional<GatewayRoute> findByMethodAndEndpoint(String httpMethod, String endpoint) {
        return Optional.empty();
    }
}
