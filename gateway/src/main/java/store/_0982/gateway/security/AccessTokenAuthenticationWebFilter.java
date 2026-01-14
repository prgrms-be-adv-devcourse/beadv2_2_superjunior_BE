package store._0982.gateway.security;

import org.springframework.http.HttpCookie;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import store._0982.gateway.domain.MemberCache;
import store._0982.gateway.infrastructure.jwt.GatewayJwtProvider;
import store._0982.gateway.infrastructure.member.MemberServiceClient;
import store._0982.gateway.security.token.AccessTokenAuthenticationToken;
import store._0982.gateway.security.token.MemberAuthenticationToken;

@Component
public class AccessTokenAuthenticationWebFilter extends AuthenticationWebFilter {

    public AccessTokenAuthenticationWebFilter(
            GatewayJwtProvider gatewayJwtProvider,
            MemberCache memberCache,
            MemberServiceClient memberServiceClient
    ) {
        super(new JwtReactiveAuthenticationManager(
                gatewayJwtProvider,
                memberCache,
                memberServiceClient
        ));
        setServerAuthenticationConverter(accessTokenAuthenticationConverter());
    }

    private ServerAuthenticationConverter accessTokenAuthenticationConverter() {
        return exchange -> {
            HttpCookie accessTokenCookie = exchange.getRequest()
                    .getCookies()
                    .getFirst("accessToken");
            if (accessTokenCookie == null || accessTokenCookie.getValue().isBlank()) {
                return Mono.just(MemberAuthenticationToken.generateGuestAuthenticationToken());
            }
            return Mono.just(new AccessTokenAuthenticationToken(accessTokenCookie.getValue()));
        };
    }
}
