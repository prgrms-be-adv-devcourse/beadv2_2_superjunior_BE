package store._0982.member.domain.member;

import store._0982.common.auth.Role;

import java.util.Optional;
import java.util.UUID;

public interface MemberRoleCache {
    Optional<Role> find(UUID memberId);

    void save(UUID memberId, Role role);

    void delete(UUID memberId);
}
