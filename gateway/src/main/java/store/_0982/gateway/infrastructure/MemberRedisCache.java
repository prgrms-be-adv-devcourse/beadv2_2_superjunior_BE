package store._0982.gateway.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import store._0982.gateway.domain.Member;
import store._0982.gateway.domain.MemberCache;
import store._0982.gateway.domain.Role;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MemberRedisCache implements MemberCache {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "member:role:cache:";

    @Override
    public Mono<Member> findById(UUID memberId) {
        return redisTemplate.opsForValue()
                .get(generateKey(memberId))
                .flatMap(roleString -> {
                    try {
                        Role role = Role.valueOf(roleString);
                        return Mono.just(Member.of(memberId, role));
                    } catch (IllegalArgumentException e) {
                        return Mono.empty();
                    }
                });
    }

    private String generateKey(UUID memberId) {
        return KEY_PREFIX + memberId;
    }
}
