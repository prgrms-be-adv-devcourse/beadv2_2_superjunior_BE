package store._0982.gateway.domain;

import java.util.Optional;
import java.util.UUID;

public interface MemberCache {
    Optional<Member> findById(UUID id);
}
