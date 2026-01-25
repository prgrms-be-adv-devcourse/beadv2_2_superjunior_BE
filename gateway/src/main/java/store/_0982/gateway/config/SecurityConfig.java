package store._0982.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.http.HttpMethod;
import store._0982.gateway.exception.CustomErrorCode;
import store._0982.gateway.exception.ExceptionHandler;
import store._0982.gateway.security.AccessTokenAuthenticationWebFilter;
import store._0982.gateway.security.RouteAuthorizationManager;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RouteAuthorizationManager routeAuthorizationManager;
    private final AccessTokenAuthenticationWebFilter authenticationWebFilter;
    private final ExceptionHandler exceptionHandler;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        authenticationWebFilter.setRequiresAuthenticationMatcher(
                ServerWebExchangeMatchers.pathMatchers(
                        "/api/**"
                )
        );

        // CORS 동작
        http.cors(corsSpec -> {});

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                // 인증 필터
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                // 인가
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 프리플라이어트
                        .pathMatchers(
                                "/auth/**",
                                "/webhooks/**",
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
                        .authenticationEntryPoint((exchange, e) -> exceptionHandler.responseException(exchange, CustomErrorCode.INVALID))
                        // 인증은 되었지만 권한이 없을 때 (403)
                        .accessDeniedHandler((exchange, e) -> exceptionHandler.responseException(exchange, CustomErrorCode.FORBIDDEN)))
                .build();
    }

}
