package store._0982.gateway.config;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import store._0982.gateway.exception.CustomErrorCode;
import store._0982.gateway.infrastructure.jwt.GatewayJwtProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = SecurityConfigTest.TestController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RouteAuthorizationManager routeAuthorizationManager;

    @MockBean
    private GatewayJwtProvider gatewayJwtProvider;

    @RestController
    static class TestController {

        @GetMapping("/swagger-ui.html")
        Mono<String> swagger() {
            return Mono.just("swagger");
        }

        @GetMapping("/api/protected")
        Mono<String> protectedEndpoint() {
            return Mono.just("protected");
        }
    }

    @Test
    void swaggerUi_isAccessibleWithoutAuthentication() {
        when(routeAuthorizationManager.check(any(), any()))
                .thenReturn(Mono.just(new AuthorizationDecision(true)));

        webTestClient.get()
                .uri("/swagger-ui.html")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("swagger");
    }

    @Test
    void protectedEndpoint_isForbiddenWhenAuthorizationManagerDeniesForAuthenticatedUser() {
        when(routeAuthorizationManager.check(any(), any()))
                .thenReturn(Mono.just(new AuthorizationDecision(false)));

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(java.util.UUID.randomUUID().toString());
        when(gatewayJwtProvider.parseToken("valid-token")).thenReturn(claims);

        webTestClient.get()
                .uri("/api/protected")
                .cookie("accessToken", "valid-token")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(String.class).isEqualTo(CustomErrorCode.FORBIDDEN.getMessage());
    }

    @Test
    void protectedEndpoint_isAccessibleWhenAuthorizationManagerAllows() {
        when(routeAuthorizationManager.check(any(), any()))
                .thenReturn(Mono.just(new AuthorizationDecision(true)));

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(java.util.UUID.randomUUID().toString());
        when(gatewayJwtProvider.parseToken("valid-token")).thenReturn(claims);

        webTestClient.get()
                .uri("/api/protected")
                .cookie("accessToken", "valid-token")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("protected");
    }
}

