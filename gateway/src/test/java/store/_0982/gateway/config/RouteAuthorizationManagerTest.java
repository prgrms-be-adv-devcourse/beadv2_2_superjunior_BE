package store._0982.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import store._0982.gateway.domain.GatewayRoute;
import store._0982.gateway.domain.GatewayRouteRepository;
import store._0982.gateway.domain.MemberCache;
import store._0982.gateway.domain.Member;
import store._0982.gateway.domain.Role;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RouteAuthorizationManagerTest {

    @Mock
    private GatewayRouteRepository gatewayRouteRepository;

    @Mock
    private MemberCache memberCache;

    @Mock
    private Authentication authentication;

    private RouteAuthorizationManager authorizationManager;

    @BeforeEach
    void setUp() {
        authorizationManager = new RouteAuthorizationManager(gatewayRouteRepository, memberCache);
    }

    @Test
    void deniesGuest_whenRouteNotConfigured() {
        var request = MockServerHttpRequest.get("/swagger-ui.html").build();
        var exchange = MockServerWebExchange.from(request);
        var context = new AuthorizationContext(exchange);

        when(gatewayRouteRepository.findByMethodAndEndpoint("GET", "/swagger-ui.html"))
                .thenReturn(Mono.empty());

        Mono<AuthorizationDecision> result = authorizationManager.check(Mono.empty(), context);

        StepVerifier.create(result)
                .assertNext(decision -> assertThat(decision.isGranted()).isFalse())
                .verifyComplete();
    }

    @Test
    void allowsGuest_whenRouteHasGuestRole() {
        var request = MockServerHttpRequest.get("/swagger-ui.html").build();
        var exchange = MockServerWebExchange.from(request);
        var context = new AuthorizationContext(exchange);

        GatewayRoute route = new GatewayRoute("GET", "/swagger-ui.html", "GUEST,CONSUMER");

        when(gatewayRouteRepository.findByMethodAndEndpoint("GET", "/swagger-ui.html"))
                .thenReturn(Mono.just(route));

        Mono<AuthorizationDecision> result = authorizationManager.check(Mono.empty(), context);

        StepVerifier.create(result)
                .assertNext(decision -> assertThat(decision.isGranted()).isTrue())
                .verifyComplete();
    }

    @Test
    void allowsAuthenticatedUser_whenRouteHasMatchingRole() {
        var request = MockServerHttpRequest.get("/api/orders").build();
        var exchange = MockServerWebExchange.from(request);
        var context = new AuthorizationContext(exchange);

        var memberId = java.util.UUID.randomUUID();
        when(authentication.getPrincipal()).thenReturn(memberId);

        Member member = mock(Member.class);
        when(member.getRole()).thenReturn(Role.CONSUMER);
        when(memberCache.findById(memberId)).thenReturn(Optional.of(member));

        GatewayRoute route = new GatewayRoute("GET", "/api/orders", "CONSUMER,SELLER");
        when(gatewayRouteRepository.findByMethodAndEndpoint("GET", "/api/orders"))
                .thenReturn(Mono.just(route));

        Mono<AuthorizationDecision> result =
                authorizationManager.check(Mono.just(authentication), context);

        StepVerifier.create(result)
                .assertNext(decision -> assertThat(decision.isGranted()).isTrue())
                .verifyComplete();
    }

    @Test
    void deniesAuthenticatedUser_whenRouteDoesNotAllowUserRole() {
        var request = MockServerHttpRequest.get("/api/orders").build();
        var exchange = MockServerWebExchange.from(request);
        var context = new AuthorizationContext(exchange);

        var memberId = java.util.UUID.randomUUID();
        when(authentication.getPrincipal()).thenReturn(memberId);

        Member member = mock(Member.class);
        when(member.getRole()).thenReturn(Role.SELLER);
        when(memberCache.findById(memberId)).thenReturn(Optional.of(member));

        GatewayRoute route = new GatewayRoute("GET", "/api/orders", "CONSUMER");
        when(gatewayRouteRepository.findByMethodAndEndpoint("GET", "/api/orders"))
                .thenReturn(Mono.just(route));

        Mono<AuthorizationDecision> result =
                authorizationManager.check(Mono.just(authentication), context);

        StepVerifier.create(result)
                .assertNext(decision -> assertThat(decision.isGranted()).isFalse())
                .verifyComplete();
    }
}
