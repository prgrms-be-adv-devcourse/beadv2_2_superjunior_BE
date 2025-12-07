package store._0982.gateway.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuthMember {
    private final String id;
    private final String email;
    private final Role role;
}

