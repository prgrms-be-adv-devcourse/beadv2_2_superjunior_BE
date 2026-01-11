package store._0982.commerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * RedisTemplate 설정
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer serializer = new StringRedisSerializer();
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);

        return template;
    }

    /**
     * 공동구매 참여 Lua Script
     */
    @Bean
    public RedisScript<Long> participateScript() {
        String script = """
                local countKey = KEYS[1]
                local idempotencyKey = KEYS[2]
                local quantity = tonumber(ARGV[1])
                local maxParticipants = tonumber(ARGV[2])
                local ttlSeconds = tonumber(ARGV[3]) -- 멱등성 키 TTL
                
                local exists = redis.call('EXISTS', idempotencyKey)
                if exists == 1 then
                    return -2 -- 이미 처리된 요청
                end
                
                local current = redis.call('GET', countKey)
                if current == false then
                    current = 0
                else
                    current = tonumber(current)
                end
                
                local afterIncrease = current + quantity
                
                if afterIncrease > maxParticipants then
                    return -1
                end
                
                local newCount = redis.call('INCRBY', countKey, quantity)
                
                redis.call('SETEX', idempotencyKey, ttlSeconds, newCount)
                
                return newCount
                """;

        return RedisScript.of(script, Long.class);
    }
}
