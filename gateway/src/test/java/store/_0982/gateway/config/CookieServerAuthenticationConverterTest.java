package store._0982.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import store._0982.gateway.infrastructure.jwt.GatewayJwtProvider;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CookieServerAuthenticationConverterTest {

    @Mock
    private GatewayJwtProvider jwtProvider;

    private CookieServerAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new CookieServerAuthenticationConverter(jwtProvider);
    }

    @Test
    void convert_returnsEmpty_whenNoAccessTokenCookie() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/some-path").build()
        );

        Mono<Authentication> result = converter.convert(exchange);

        StepVerifier.create(result)
                .verifyComplete();

        assertThat(exchange.getAttributes()).doesNotContainKey("memberId");
    }

    @Test
    void convert_returnsAuthentication_andStoresMemberId_whenValidToken() {
        UUID memberId = UUID.randomUUID();

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(memberId.toString());
        when(jwtProvider.parseToken("valid-token")).thenReturn(claims);

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/some-path")
                        .cookie(new HttpCookie("accessToken", "valid-token"))
                        .build()
        );

        Mono<Authentication> result = converter.convert(exchange);

        StepVerifier.create(result)
                .assertNext(auth -> {
                    assertThat(auth.isAuthenticated()).isTrue();
                    assertThat(auth.getPrincipal()).isEqualTo(memberId);
                })
                .verifyComplete();

        UUID storedMemberId = exchange.getAttribute("memberId");
        assertThat(storedMemberId).isEqualTo(memberId);
    }

    @Test
    void convert_returnsEmpty_whenJwtInvalid() {
        when(jwtProvider.parseToken("invalid-token"))
                .thenThrow(new JwtException("invalid"));

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/some-path")
                        .cookie(new HttpCookie("accessToken", "invalid-token"))
                        .build()
        );

        Mono<Authentication> result = converter.convert(exchange);

        StepVerifier.create(result)
                .verifyComplete();

        assertThat(exchange.getAttributes()).doesNotContainKey("memberId");
    }

    @Test
    void convert_returnsEmpty_whenJwtExpired() {
        when(jwtProvider.parseToken("expired-token"))
                .thenThrow(new ExpiredJwtException(null, null, "expired"));

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/some-path")
                        .cookie(new HttpCookie("accessToken", "expired-token"))
                        .build()
        );

        Mono<Authentication> result = converter.convert(exchange);

        StepVerifier.create(result)
                .verifyComplete();

        assertThat(exchange.getAttributes()).doesNotContainKey("memberId");
    }
}
