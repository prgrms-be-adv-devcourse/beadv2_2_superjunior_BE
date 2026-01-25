package store._0982.gateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import store._0982.gateway.domain.Role;
import store._0982.gateway.exception.CustomErrorCode;
import store._0982.gateway.exception.ExceptionHandler;
import store._0982.gateway.security.token.MemberAuthenticationToken;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestAwareAccessDeniedHandlerTest {

    @Mock
    private ExceptionHandler exceptionHandler;

    @Mock
    private ServerWebExchange exchange;

    @InjectMocks
    private GuestAwareAccessDeniedHandler handler;

    private AccessDeniedException accessDeniedException;

    @BeforeEach
    void setUp() {
        accessDeniedException = new AccessDeniedException("Access Denied");
    }

    @Test
    @DisplayName("GUEST 역할은 401 UNAUTHORIZED를 반환한다")
    void guestRole_returns401() {
        MemberAuthenticationToken guestToken = MemberAuthenticationToken.generateGuestAuthenticationToken();
        when(exchange.getPrincipal()).thenReturn(Mono.just(guestToken));
        when(exceptionHandler.responseException(any(), any()))
            .thenReturn(Mono.fromRunnable(() -> {}));

        Mono<Void> result = handler.handle(exchange, accessDeniedException);

        StepVerifier.create(result)
            .verifyComplete();

        verify(exceptionHandler).responseException(exchange, CustomErrorCode.UNAUTHENTICATED);
        verify(exceptionHandler, never()).responseException(exchange, CustomErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("CONSUMER 역할은 403 FORBIDDEN을 반환한다")
    void consumerRole_returns403() {
        MemberAuthenticationToken consumerToken = new MemberAuthenticationToken(
            UUID.randomUUID(),
            Role.CONSUMER,
            null
        );
        when(exchange.getPrincipal()).thenReturn(Mono.just(consumerToken));
        when(exceptionHandler.responseException(any(), any()))
            .thenReturn(Mono.fromRunnable(() -> {}));

        Mono<Void> result = handler.handle(exchange, accessDeniedException);

        StepVerifier.create(result)
            .verifyComplete();

        verify(exceptionHandler).responseException(exchange, CustomErrorCode.FORBIDDEN);
        verify(exceptionHandler, never()).responseException(exchange, CustomErrorCode.UNAUTHENTICATED);
    }

    @Test
    @DisplayName("SELLER 역할은 403 FORBIDDEN을 반환한다")
    void sellerRole_returns403() {
        MemberAuthenticationToken sellerToken = new MemberAuthenticationToken(
            UUID.randomUUID(),
            Role.SELLER,
            null
        );
        when(exchange.getPrincipal()).thenReturn(Mono.just(sellerToken));
        when(exceptionHandler.responseException(any(), any()))
            .thenReturn(Mono.fromRunnable(() -> {}));

        Mono<Void> result = handler.handle(exchange, accessDeniedException);

        StepVerifier.create(result)
            .verifyComplete();

        verify(exceptionHandler).responseException(exchange, CustomErrorCode.FORBIDDEN);
        verify(exceptionHandler, never()).responseException(exchange, CustomErrorCode.UNAUTHENTICATED);
    }

    @Test
    @DisplayName("Principal이 없으면 403 FORBIDDEN을 반환한다")
    void noPrincipal_returns403() {
        when(exchange.getPrincipal()).thenReturn(Mono.empty());
        when(exceptionHandler.responseException(any(), any()))
            .thenReturn(Mono.fromRunnable(() -> {}));

        Mono<Void> result = handler.handle(exchange, accessDeniedException);

        StepVerifier.create(result)
            .verifyComplete();

        verify(exceptionHandler).responseException(eq(exchange), any(CustomErrorCode.class));
        verify(exceptionHandler, never()).responseException(exchange, CustomErrorCode.UNAUTHENTICATED);
    }

    @Test
    @DisplayName("Principal이 MemberAuthenticationToken이 아니면 403 FORBIDDEN을 반환한다")
    void wrongPrincipalType_returns403() {
        Authentication otherAuth = mock(AbstractAuthenticationToken.class);
        when(exchange.getPrincipal()).thenReturn(Mono.just(otherAuth));
        when(exceptionHandler.responseException(any(), any()))
            .thenReturn(Mono.fromRunnable(() -> {}));

        Mono<Void> result = handler.handle(exchange, accessDeniedException);

        StepVerifier.create(result)
            .verifyComplete();

        verify(exceptionHandler).responseException(exchange, CustomErrorCode.FORBIDDEN);
        verify(exceptionHandler, never()).responseException(exchange, CustomErrorCode.UNAUTHENTICATED);
    }
}
