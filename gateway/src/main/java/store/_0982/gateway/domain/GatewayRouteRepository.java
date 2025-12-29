package store._0982.gateway.domain;

import java.util.Optional;

public interface GatewayRouteRepository {
    Optional<GatewayRoute> findByMethodAndEndpoint(String httpMethod, String endpoint);
}
