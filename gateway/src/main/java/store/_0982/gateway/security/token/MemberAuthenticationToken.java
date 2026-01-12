package store._0982.gateway.security.token;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import store._0982.gateway.domain.Role;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * 인증 완료 후 memberId와 role을 보유하는 인증 토큰.
 */
public class MemberAuthenticationToken extends AbstractAuthenticationToken {

    private final UUID memberId;
    @Getter
    private final Role role;

    public MemberAuthenticationToken(UUID memberId, Role role, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.memberId = memberId;
        this.role = role;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return memberId;
    }

    public static MemberAuthenticationToken generateGuestAuthenticationToken() {
        return new MemberAuthenticationToken(UUID.fromString("00000000-0000-0000-0000-000000000000"), Role.GUEST, Collections.singletonList(new SimpleGrantedAuthority(Role.GUEST.name())));
    }
}
