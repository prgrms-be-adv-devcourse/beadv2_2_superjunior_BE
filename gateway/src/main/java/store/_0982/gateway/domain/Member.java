package store._0982.gateway.domain;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member {
    private UUID memberId;
    private Role role;

    public static Member createGuest(){
        return new Member(null, Role.GUEST);
    }
}
