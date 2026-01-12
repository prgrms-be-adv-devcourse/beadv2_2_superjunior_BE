package store._0982.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.http.HttpCookie;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import reactor.core.publisher.Mono;
import store._0982.gateway.domain.MemberCache;
import store._0982.gateway.exception.CustomErrorCode;
import store._0982.gateway.exception.ExceptionHandler;
import store._0982.gateway.infrastructure.jwt.GatewayJwtProvider;
import store._0982.gateway.infrastructure.member.MemberServiceClient;
import store._0982.gateway.security.token.AccessTokenAuthenticationToken;
import store._0982.gateway.security.JwtReactiveAuthenticationManager;
import store._0982.gateway.security.token.MemberAuthenticationToken;
import store._0982.gateway.security.RouteAuthorizationManager;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RouteAuthorizationManager routeAuthorizationManager;
    private final GatewayJwtProvider gatewayJwtProvider;
    private final MemberCache memberCache;
    private final MemberServiceClient memberServiceClient;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        AuthenticationWebFilter authenticationWebFilter = authenticationWebFilter();

        return http.csrf(ServerHttpSecurity.CsrfSpec::disable).httpBasic(ServerHttpSecurity.HttpBasicSpec::disable).formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                // 인증 필터
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                // 인가
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/actuator/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/**/v3/api-docs"
                        ).permitAll()
                        // 라우팅 별 권한 체크
                        .anyExchange().access(routeAuthorizationManager)
                ).exceptionHandling(ex -> ex
                        // 인증이 아예 없을 때 (401) 만료된 토큰, 이상한 토큰
                        .authenticationEntryPoint((exchange, e) -> ExceptionHandler.responseException(exchange, CustomErrorCode.INVALID))
                        // 인증은 되었지만 권한이 없을 때 (403)
                        .accessDeniedHandler((exchange, e) -> ExceptionHandler.responseException(exchange, CustomErrorCode.FORBIDDEN)))
                .build();
    }

    @Bean
    public AuthenticationWebFilter authenticationWebFilter() {
        ReactiveAuthenticationManager authenticationManager = new JwtReactiveAuthenticationManager(
                gatewayJwtProvider,
                memberCache,
                memberServiceClient
        );
        ServerAuthenticationConverter converter = exchange -> {
            HttpCookie accessTokenCookie = exchange.getRequest()
                    .getCookies()
                    .getFirst("accessToken");
            if (accessTokenCookie == null || accessTokenCookie.getValue().isBlank()) {
                return Mono.just(MemberAuthenticationToken.generateGuestAuthenticationToken());
            }
            return Mono.just(new AccessTokenAuthenticationToken(accessTokenCookie.getValue()));
        };

        AuthenticationWebFilter filter = new AuthenticationWebFilter(authenticationManager);
        filter.setServerAuthenticationConverter(converter);
        return filter;
    }
}
