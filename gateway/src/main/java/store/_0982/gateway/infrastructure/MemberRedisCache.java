package store._0982.gateway.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import store._0982.gateway.domain.Member;
import store._0982.gateway.domain.MemberCache;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MemberRedisCache implements MemberCache {

    private final ReactiveRedisTemplate<String, Member> redisTemplate;

    @Override
    public Optional<Member> findById(UUID id) {
        Mono<Member> mono = redisTemplate.opsForValue().get(buildKey(id));
        return mono.blockOptional();
    }

    private String buildKey(UUID id) {
        return "member:" + id;
    }
}
