package store._0982.recommendation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import store._0982.recommendation.application.dto.RecommendInfo;

import java.util.concurrent.TimeUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisCacheService implements CacheService {
    private static final String PRODUCT_LIST_CACHE_KEY_PREFIX = "recommendation:recommend-info:";

    private final RedisTemplate<String, RecommendInfo> redisTemplate;

    // 단일 추천 결과를 Value로 캐싱
    @Override
    public RecommendInfo getRecommendationList(UUID memberId) {
        String key = getKey(memberId);
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void putRecommendationList(UUID memberId, RecommendInfo recommendInfo) {
        String key = getKey(memberId);
        if (recommendInfo == null) {
            redisTemplate.delete(key);
            return;
        }

        redisTemplate.opsForValue().set(key, recommendInfo, TTL_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public Long getTtlOfKey(UUID memberId) {
        return getTtlOfKey(getKey(memberId));
    }

    private Long getTtlOfKey(String key) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl == null ? -2L : ttl;
    }

    private String getKey(UUID memberId) {
        return PRODUCT_LIST_CACHE_KEY_PREFIX + memberId;
    }
}
