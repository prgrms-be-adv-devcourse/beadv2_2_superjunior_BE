package store._0982.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import store._0982.gateway.domain.Member;
import store._0982.gateway.domain.MemberCache;
import store._0982.gateway.domain.Role;
import store._0982.gateway.infrastructure.jwt.GatewayJwtProvider;
import store._0982.gateway.infrastructure.member.MemberServiceClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtReactiveAuthenticationManagerTest {

    @Mock
    private GatewayJwtProvider jwtProvider;

    @Mock
    private MemberCache memberCache;

    @Mock
    private MemberServiceClient memberServiceClient;

    private JwtReactiveAuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        authenticationManager = new JwtReactiveAuthenticationManager(jwtProvider, memberCache, memberServiceClient);
    }

    @Test
    void returnsEmpty_whenAuthenticationTypeDoesNotMatch() {
        Authentication unsupported = mock(Authentication.class);

        StepVerifier.create(authenticationManager.authenticate(unsupported))
                .verifyComplete();
    }

    @Test
    void authenticatesAndLoadsRoleFromCache_whenTokenValid() {
        String token = "valid-token";
        UUID memberId = UUID.randomUUID();

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(memberId.toString());
        when(jwtProvider.parseToken(token)).thenReturn(claims);

        Member member = Member.of(memberId, Role.CONSUMER);
        when(memberCache.findById(memberId)).thenReturn(Mono.just(member));

        Authentication input = new AccessTokenAuthenticationToken(token);

        StepVerifier.create(authenticationManager.authenticate(input))
                .assertNext(auth -> {
                    assertThat(auth).isInstanceOf(MemberAuthenticationToken.class);
                    assertThat(auth.isAuthenticated()).isTrue();
                    assertThat(auth.getPrincipal()).isEqualTo(memberId);
                    assertThat(auth.getAuthorities()).containsExactly(new SimpleGrantedAuthority(Role.CONSUMER.name()));
                })
                .verifyComplete();
    }

    @Test
    void fetchesFromMemberService_whenMemberNotInCache() {
        String token = "valid-token";
        UUID memberId = UUID.randomUUID();

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(memberId.toString());
        when(jwtProvider.parseToken(token)).thenReturn(claims);

        when(memberCache.findById(memberId)).thenReturn(Mono.empty());
        Member remoteMember = Member.of(memberId, Role.SELLER);
        when(memberServiceClient.fetchMember(memberId)).thenReturn(Mono.just(remoteMember));
        when(memberCache.save(remoteMember)).thenReturn(Mono.empty());

        Authentication input = new AccessTokenAuthenticationToken(token);

        StepVerifier.create(authenticationManager.authenticate(input))
                .assertNext(auth -> {
                    assertThat(auth.getPrincipal()).isEqualTo(memberId);
                    assertThat(auth.getAuthorities()).containsExactly(new SimpleGrantedAuthority(Role.SELLER.name()));
                })
                .verifyComplete();
    }

    @Test
    void emitsError_whenMemberServiceDoesNotReturnRole() {
        String token = "valid-token";
        UUID memberId = UUID.randomUUID();

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(memberId.toString());
        when(jwtProvider.parseToken(token)).thenReturn(claims);

        when(memberCache.findById(memberId)).thenReturn(Mono.empty());
        when(memberServiceClient.fetchMember(memberId)).thenReturn(Mono.empty());

        Authentication input = new AccessTokenAuthenticationToken(token);

        StepVerifier.create(authenticationManager.authenticate(input))
                .expectError(JwtException.class)
                .verify();
    }

    @Test
    void emitsError_whenTokenInvalid() {
        String token = "invalid-token";
        when(jwtProvider.parseToken(token)).thenThrow(new JwtException("invalid"));

        Authentication input = new AccessTokenAuthenticationToken(token);

        StepVerifier.create(authenticationManager.authenticate(input))
                .expectError(JwtException.class)
                .verify();
    }
}
