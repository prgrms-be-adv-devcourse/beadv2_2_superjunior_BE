package store._0982.recommendation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisCacheService implements CacheService {
    private static final String PRODUCT_LIST_CACHE_KEY_PREFIX = "recommendation:product-list:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<UUID> refreshProductListCache(UUID memberId) {
        return List.of();
    }

    @Override
    public List<UUID> getProductListCache(UUID memberId) {
        String key = getProductListCacheKey(memberId);
        long ttlSeconds = getTtlOfKey(key);

        if (ttlSeconds == -2L) {
            return refreshProductListCache(memberId);
        }

        List<String> cachedProductIds = stringRedisTemplate.opsForList().range(key, 0, -1);
        if (cachedProductIds == null || cachedProductIds.isEmpty()) {
            return refreshProductListCache(memberId);
        }

        return cachedProductIds.stream()
                .map(this::toUuid)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private long getTtlOfKey(String key) {
        return Optional.ofNullable(stringRedisTemplate.getExpire(key, TimeUnit.SECONDS))
                .orElse(-2L);
    }

    private String getProductListCacheKey(UUID memberId) {
        return PRODUCT_LIST_CACHE_KEY_PREFIX + memberId;
    }

    private Optional<UUID> toUuid(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
