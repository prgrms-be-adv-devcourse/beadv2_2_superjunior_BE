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
import store._0982.gateway.security.GuestAwareAccessDeniedHandler;
import store._0982.gateway.security.RouteAuthorizationManager;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RouteAuthorizationManager routeAuthorizationManager;
    private final AccessTokenAuthenticationWebFilter authenticationWebFilter;
    private final ExceptionHandler exceptionHandler;
    private final GuestAwareAccessDeniedHandler guestAwareAccessDeniedHandler;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        authenticationWebFilter.setRequiresAuthenticationMatcher(
                ServerWebExchangeMatchers.pathMatchers(
                        "/api/**"
                )
        );

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
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
                                "/**/v3/api-docs",
                                "/oauth2/authorization/**"
                        ).permitAll()
                        // 라우팅 별 권한 체크
                        .anyExchange().access(routeAuthorizationManager)
                ).exceptionHandling(ex -> ex
                        .authenticationEntryPoint((exchange, e) -> exceptionHandler.responseException(exchange, CustomErrorCode.INVALID))
                        .accessDeniedHandler(guestAwareAccessDeniedHandler))
                .build();
    }

}
