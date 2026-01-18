package store._0982.member.infrastructure.member;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import store._0982.common.auth.Role;
import store._0982.member.domain.member.MemberRoleCache;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MemberRoleRedisCache implements MemberRoleCache {

    private static final String KEY_PREFIX = "member:role:cache:";
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Optional<Role> find(UUID memberId) {
        String key = generateKey(memberId);
        String value = redisTemplate.opsForValue().get(key);

        if(value == null) return Optional.empty();
        return Optional.of(Role.valueOf(value));
    }

    @Override
    public void save(UUID memberId, Role role) {
        String key = generateKey(memberId);
        String value = role.name();

        redisTemplate.opsForValue().set(key, value, Duration.ofHours(1));
    }

    @Override
    public void delete(UUID memberId) {
        String key = generateKey(memberId);
        redisTemplate.delete(key);
    }

    private String generateKey(UUID memberId){
        return KEY_PREFIX + memberId.toString();
    }
}
