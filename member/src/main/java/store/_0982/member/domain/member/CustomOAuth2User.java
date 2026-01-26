package store._0982.member.domain.member;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class CustomOAuth2User implements OAuth2User {
    private final UUID memberId;
    private final String email;
    private final String displayName;
    private final String role;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomOAuth2User(UUID memberId,
                            String email,
                            String displayName,
                            String role,
                            Map<String, Object> attributes) {
        this.memberId = memberId;
        this.email = email;
        this.displayName = displayName;
        this.role = role;
        this.attributes = Map.copyOf(attributes);
        this.authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role),
                new SimpleGrantedAuthority(role)
        );
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return email;
    }
}
