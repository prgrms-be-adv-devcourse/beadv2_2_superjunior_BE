package store._0982.gateway.infrastructure;

import store._0982.gateway.domain.Member;
import store._0982.gateway.domain.MemberRepository;

import java.util.Optional;
import java.util.UUID;

public class MemberRedisRepository implements MemberRepository {
    @Override
    public Optional<Member> findById(UUID id) {
        return Optional.empty();
    }
}
