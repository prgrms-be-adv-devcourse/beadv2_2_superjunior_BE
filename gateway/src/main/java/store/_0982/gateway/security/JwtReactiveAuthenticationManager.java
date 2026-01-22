package store._0982.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;
import store._0982.gateway.domain.Role;
import store._0982.gateway.infrastructure.jwt.GatewayJwtProvider;
import store._0982.gateway.security.token.AccessTokenAuthenticationToken;
import store._0982.gateway.security.token.MemberAuthenticationToken;

import java.util.Collections;
import java.util.UUID;

/**
 * JWT 검증 및 member 역할 조회를 수행하는 ReactiveAuthenticationManager.
 */
@RequiredArgsConstructor
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final GatewayJwtProvider jwtProvider;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {

        if (authentication instanceof MemberAuthenticationToken) {
            return Mono.just(authentication); // 게스트 토큰 등 이미 인증된 토큰은 그대로 사용
        }

        if (!(authentication instanceof AccessTokenAuthenticationToken)) {
            return Mono.empty();
        }

        String token = (String) authentication.getCredentials();
        if (token == null || token.isBlank()) {
            return Mono.empty();
        }

        return Mono.fromCallable(() -> jwtProvider.parseToken(token))
                .map(GatewayJwtProvider::toMember)
                .map(member -> toAuthenticatedToken(member.getMemberId(), member.getRole()));
    }

    private MemberAuthenticationToken toAuthenticatedToken(UUID memberId, Role role) {
        return new MemberAuthenticationToken(
                memberId,
                role,
                Collections.singletonList(new SimpleGrantedAuthority(role.name()))
        );
    }
}
