package dev.adi.customimpl.ratelimiter.impl;

import static dev.adi.customimpl.ratelimiter.enums.RateLimitingAlgorithm.TOKEN_BUCKET;

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
public class TokenBucketRateLimiter implements RateLimiter {
    private static final String TOKEN_WINDOW_KEY = "ratelimit:tokenbucket:%s";
    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Long> tokenWindowScript;

    public TokenBucketRateLimiter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.tokenWindowScript = new DefaultRedisScript<>();
    }

    @Override
    public boolean isAllowed(String key) {
        long current = Instant.now().toEpochMilli();

        Long result = redisTemplate.execute(
                tokenWindowScript,
                Collections.singletonList(String.format(TOKEN_WINDOW_KEY, key)),
                current, MAX_REQUESTS_ALLOWED_PER_10_MINUTES, 10);

        return result != null && result == 1;
    }

    @PostConstruct
    public void setScripts() {
        this.tokenWindowScript.setResultType(Long.class);
        this.tokenWindowScript.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("lua-scripts/token-window-script.lua")));
    }

    @Override
    public RateLimitingAlgorithm getAlgorithm() {
       return TOKEN_BUCKET;
    }
}