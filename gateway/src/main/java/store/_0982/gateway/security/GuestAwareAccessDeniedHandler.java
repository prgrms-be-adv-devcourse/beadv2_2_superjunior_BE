package store._0982.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import store._0982.gateway.domain.Role;
import store._0982.gateway.exception.CustomErrorCode;
import store._0982.gateway.exception.ExceptionHandler;
import store._0982.gateway.security.token.MemberAuthenticationToken;

@Component
@RequiredArgsConstructor
public class GuestAwareAccessDeniedHandler implements ServerAccessDeniedHandler {

    private final ExceptionHandler exceptionHandler;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        return exchange.getPrincipal()
                .flatMap(principal -> {
                    if (!(principal instanceof MemberAuthenticationToken token)) {
                        return exceptionHandler.responseException(exchange, CustomErrorCode.FORBIDDEN);
                    }

                    if (token.getRole() == Role.GUEST) {
                        return exceptionHandler.responseException(exchange, CustomErrorCode.UNAUTHENTICATED);
                    }
                    return exceptionHandler.responseException(exchange, CustomErrorCode.FORBIDDEN);
                });
    }
}
