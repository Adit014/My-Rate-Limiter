package dev.adi.customimpl.ratelimiter.impl;

import static dev.adi.customimpl.ratelimiter.enums.RateLimitingAlgorithm.LEAKY_BUCKET;

import java.time.Instant;
import java.util.Collections;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import dev.adi.customimpl.ratelimiter.RateLimiter;
import dev.adi.customimpl.ratelimiter.enums.RateLimitingAlgorithm;
import jakarta.annotation.PostConstruct;

@Service
public class LeakyBucketRateLimiter implements RateLimiter{
private static final String LEAKY_BUCKET_KEY = "ratelimit:leakybucket:%s";
    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Long> leakyBucketScript;

    public LeakyBucketRateLimiter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.leakyBucketScript = new DefaultRedisScript<>();
    }

    @Override
    public boolean isAllowed(String key) {
        long current = Instant.now().toEpochMilli();

        Long result = redisTemplate.execute(
                leakyBucketScript,
                Collections.singletonList(String.format(LEAKY_BUCKET_KEY, key)),
                current, MAX_REQUESTS_ALLOWED_PER_10_MINUTES, 10);

        return result != null && result == 1;
    }

    @PostConstruct
    public void setScripts() {
        this.leakyBucketScript.setResultType(Long.class);
        this.leakyBucketScript.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("lua-scripts/leaky-bucket-script.lua")));
    }

    @Override
    public RateLimitingAlgorithm getAlgorithm() {
        return LEAKY_BUCKET;
    }
}
