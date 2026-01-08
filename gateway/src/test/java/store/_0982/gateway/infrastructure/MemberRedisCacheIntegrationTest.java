package store._0982.gateway.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;
import store._0982.gateway.AbstractIntegrationTest;
import store._0982.gateway.domain.Member;
import store._0982.gateway.domain.Role;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MemberRedisCacheIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MemberRedisCache memberRedisCache;

    @Autowired
    private ReactiveRedisTemplate<String, Member> redisTemplate;

    @Test
    void findById_returnsMemberWhenPresentInRedis() {
        UUID memberId = UUID.randomUUID();
        Member member = Member.createGuest();

        redisTemplate.opsForValue().set("member:" + memberId, member).block();

        Member result = memberRedisCache.findById(memberId).block();

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(Role.GUEST);
    }

    @Test
    void findById_returnsEmptyWhenMemberNotInRedis() {
        UUID memberId = UUID.randomUUID();

        Optional<Member> result = memberRedisCache.findById(memberId).blockOptional();

        assertThat(result).isEmpty();
    }

}
