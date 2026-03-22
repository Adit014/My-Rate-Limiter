package dev.adi.customimpl.ratelimiter.impl;

import static dev.adi.customimpl.ratelimiter.enums.RateLimitingAlgorithm.SLIDING_WINDOW_LOG;

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
public class SlidingWindowLogRateLimiter implements RateLimiter {
    private static final String SLIDING_WINDOW_LOG_REDIS_KEY = "ratelimit:slidinglog:%s";
    private static final long WINDOW_SIZE_MS = 10 * 60 * 1000L;
    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Long> slidingWindowScript;

    public SlidingWindowLogRateLimiter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.slidingWindowScript = new DefaultRedisScript<>();
    }

    @Override
    public boolean isAllowed(String key) {
        String redisKey = String.format(SLIDING_WINDOW_LOG_REDIS_KEY, key);
        long currentMillis = Instant.now().toEpochMilli();
        long windowStart = currentMillis - WINDOW_SIZE_MS;

        Long result = redisTemplate.execute(
                slidingWindowScript,
                Collections.singletonList(redisKey),
                windowStart, currentMillis, MAX_REQUESTS_ALLOWED_PER_10_MINUTES);

        return result != null && result == 1;
    }

    @PostConstruct
    public void setScripts() {
        this.slidingWindowScript.setResultType(Long.class);
        this.slidingWindowScript.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("lua-scripts/sliding-window-log-script.lua")));
    }

    @Override
    public RateLimitingAlgorithm getAlgorithm() {
        return SLIDING_WINDOW_LOG;
    }

}