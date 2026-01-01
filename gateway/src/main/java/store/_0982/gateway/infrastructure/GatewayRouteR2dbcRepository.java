package store._0982.gateway.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import store._0982.gateway.domain.GatewayRoute;
import store._0982.gateway.domain.GatewayRouteRepository;

@Repository
@RequiredArgsConstructor
public class GatewayRouteR2dbcRepository implements GatewayRouteRepository {

    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Override
    public Mono<GatewayRoute> findByMethodAndEndpoint(String httpMethod, String endpoint) {
        Query query = Query.query(
                Criteria.where("httpMethod").is(httpMethod)
                        .and("endpoint").is(endpoint)
        );

        return r2dbcEntityTemplate
                .select(GatewayRoute.class)
                .matching(query)
                .first();
    }
}
