package store._0982.gateway.infrastructure;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import store._0982.gateway.domain.GatewayRoute;

import java.util.UUID;

interface GatewayRouteReactiveCrudRepository extends ReactiveCrudRepository<GatewayRoute, UUID> {

    @Query("""
        SELECT *
        FROM gateway_schema.gateway_route gr
        WHERE $1 = gr.http_method and $2 ~ gr.endpoint
    """)
    public Mono<GatewayRoute> findByMethodAndEndpoint(String httpMethod, String endpoint);

}
