package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.common.domain.grouppurchase.GroupPurchase;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GroupPurchaseCounterService {

    private static final String COUNT_KEY_PREFIX = "gp:";
    private static final String COUNT_KEY_SUFFIX = ":count";
    private static final String MAX_KEY_SUFFIX = ":max";

    private static final DefaultRedisScript<Long> RESERVE_SCRIPT = new DefaultRedisScript<>(
            """
            local count = tonumber(redis.call('GET', KEYS[1]) or '0')
            local max = tonumber(redis.call('GET', KEYS[2]) or '0')
            local delta = tonumber(ARGV[1])
            if max == 0 then
              return -2
            end
            if (count + delta) > max then
              return -1
            end
            return redis.call('INCRBY', KEYS[1], delta)
            """,
            Long.class
    );

    private static final DefaultRedisScript<Long> ROLLBACK_SCRIPT = new DefaultRedisScript<>(
            """
            local count = tonumber(redis.call('GET', KEYS[1]) or '0')
            local delta = tonumber(ARGV[1])
            if count < delta then
              return -1
            end
            return redis.call('DECRBY', KEYS[1], delta)
            """,
            Long.class
    );

    private final RedisTemplate<String, String> redisTemplate;
    private final GroupPurchaseRepository groupPurchaseRepository;

    public int reserve(GroupPurchase groupPurchase, int quantity){
        try{
            ensureInitialized(groupPurchase);
            String countKey = countKey(groupPurchase.getGroupPurchaseId());
            String maxKey = maxKey(groupPurchase.getGroupPurchaseId());

            Long result = redisTemplate.execute(
                    RESERVE_SCRIPT,
                    List.of(countKey, maxKey),
                    String.valueOf(quantity)
            );
            if (result == null) {
                return -1;
            }
            return result.intValue();
        } catch (RuntimeException e) {
            GroupPurchase updated = groupPurchaseRepository.increaseQuantityReturning(
                    groupPurchase.getGroupPurchaseId(),
                    quantity
            );
            if (updated == null) {
                return -1;
            }
            return updated.getCurrentQuantity();
        }
    }

    public int rollback(UUID groupPurchaseId, int quantity){
        String countKey = countKey(groupPurchaseId);
        try{
            Long result = redisTemplate.execute(
                    ROLLBACK_SCRIPT,
                    List.of(countKey),
                    String.valueOf(quantity)
            );

            return result == null ? -1 : result.intValue();
        } catch (RuntimeException e){
            int updated = groupPurchaseRepository.decreaseQuantity(groupPurchaseId, quantity);
            return updated == 0 ? -1 : 0;
        }
    }

    public int getCurrent(GroupPurchase groupPurchase){
        try{
            ensureInitialized(groupPurchase);
            String countKey = countKey(groupPurchase.getGroupPurchaseId());
            String value = redisTemplate.opsForValue().get(countKey);

            if(value == null){
                return groupPurchase.getCurrentQuantity();
            }
            return Integer.parseInt(value);
        } catch (RuntimeException e){
            return groupPurchase.getCurrentQuantity();
        }
    }

    public Map<UUID, Integer> getCurrentCounts(List<GroupPurchase> groupPurchases){
        if(groupPurchases.isEmpty()){
            return Map.of();
        }

        List<String> keys = new ArrayList<>(groupPurchases.size());

        for(GroupPurchase gp : groupPurchases){
            keys.add(countKey(gp.getGroupPurchaseId()));
        }

        List<String> values;

        try{
            values = redisTemplate.opsForValue().multiGet(keys);
        } catch (RuntimeException e){
            values = null;
        }

        Map<UUID, Integer> result = new HashMap<>(groupPurchases.size());
        for(int i=0;i<groupPurchases.size();i++){
            GroupPurchase gp = groupPurchases.get(i);
            String value = values == null ? null : values.get(i);

            if(value == null){
                ensureInitialized(gp);
                value = redisTemplate.opsForValue().get(countKey(gp.getGroupPurchaseId()));
            }

            int count = value == null ? gp.getCurrentQuantity() : Integer.parseInt(value);
            result.put(gp.getGroupPurchaseId(), count);
        }
        return result;

    }

    public void updateSuccessIfReached(GroupPurchase groupPurchase, int currentCount){
        if(currentCount >= groupPurchase.getMaxQuantity()){
            groupPurchaseRepository.updateStatusToSuccess(groupPurchase.getGroupPurchaseId(), currentCount);
        }
    }

    private void ensureInitialized(GroupPurchase groupPurchase){
        UUID id = groupPurchase.getGroupPurchaseId();
        String countKey = countKey(id);
        String maxKey = maxKey(id);

        redisTemplate.opsForValue().setIfAbsent(countKey, String.valueOf(groupPurchase.getCurrentQuantity()));
        redisTemplate.opsForValue().setIfAbsent(maxKey, String.valueOf(groupPurchase.getMaxQuantity()));
    }

    private String countKey(UUID groupPurchaseId) {
        return COUNT_KEY_PREFIX + groupPurchaseId + COUNT_KEY_SUFFIX;
    }

    private String maxKey(UUID groupPurchaseId) {
        return COUNT_KEY_PREFIX + groupPurchaseId + MAX_KEY_SUFFIX;
    }
}
