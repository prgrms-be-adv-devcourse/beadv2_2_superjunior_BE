package store._0982.gateway.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import store._0982.gateway.AbstractIntegrationTest;
import store._0982.gateway.domain.GatewayRoute;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayRouteR2dbcRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private GatewayRouteR2dbcRepository repository;

    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @BeforeEach
    void setUpSchema() {
        r2dbcEntityTemplate.getDatabaseClient()
                .sql("CREATE SCHEMA IF NOT EXISTS gateway_schema")
                .then()
                .block();

        r2dbcEntityTemplate.getDatabaseClient()
                .sql("""
                        CREATE TABLE IF NOT EXISTS gateway_schema.gateway_route (
                            id UUID PRIMARY KEY,
                            http_method VARCHAR(10) NOT NULL,
                            endpoint VARCHAR(255) NOT NULL,
                            roles VARCHAR(255) NOT NULL
                        )
                        """)
                .then()
                .block();

        r2dbcEntityTemplate.getDatabaseClient()
                .sql("TRUNCATE TABLE gateway_schema.gateway_route")
                .then()
                .block();
    }

    @Test
    void findByMethodAndEndpoint_returnsRouteWhenExists() {
        UUID id = UUID.randomUUID();
        GatewayRoute route = new GatewayRoute(id, "GET", "/api/orders", "CONSUMER,SELLER");

        r2dbcEntityTemplate.insert(GatewayRoute.class)
                .using(route)
                .block();

        Mono<GatewayRoute> result = repository.findByMethodAndEndpoint("GET", "/api/orders");

        StepVerifier.create(result)
                .assertNext(found -> {
                    assertThat(found.getId()).isEqualTo(id);
                    assertThat(found.getHttpMethod()).isEqualTo("GET");
                    assertThat(found.getEndpoint()).isEqualTo("/api/orders");
                    assertThat(found.getRoles()).isEqualTo("CONSUMER,SELLER");
                })
                .verifyComplete();
    }

    @Test
    void findByMethodAndEndpoint_returnsEmptyWhenNotFound() {
        Mono<GatewayRoute> result = repository.findByMethodAndEndpoint("GET", "/not-exists");

        StepVerifier.create(result)
                .verifyComplete();
    }
}

