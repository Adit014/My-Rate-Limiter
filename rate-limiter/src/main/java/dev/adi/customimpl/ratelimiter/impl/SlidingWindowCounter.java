package dev.adi.customimpl.ratelimiter.impl;

import java.time.Duration;
import java.time.Instant;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import dev.adi.customimpl.ratelimiter.RateLimiter;
import dev.adi.customimpl.ratelimiter.enums.RateLimitingAlgorithm;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SlidingWindowCounter implements RateLimiter {
    private static final String SLIDING_WINDOW_COUNTER = "ratelimit:slidingcounter:%s:%d";
    private static final Duration EXPIRATION_IN_MINUTES = Duration.ofMinutes(20);
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean isAllowed(String key) {
        Instant currentTime = Instant.now();
        Long differenceInMinutes = Duration.between(Instant.EPOCH, currentTime).toMinutes();
        long previousSlotCarryOverMinutes = 10 - (differenceInMinutes % 10);
        long currentSlotId = differenceInMinutes / 10;
        long previousSlotId = currentSlotId - 1;

        Long previousSlotValue = (Long) redisTemplate.opsForValue().get(String.format(SLIDING_WINDOW_COUNTER, key, previousSlotId));
        previousSlotValue = previousSlotValue == null ? 0L : previousSlotValue;
        double overlapRatio = previousSlotCarryOverMinutes / 10.0;
        long previousCarryOver = Math.round(previousSlotValue * overlapRatio);

        String currentKey = String.format(SLIDING_WINDOW_COUNTER, key, currentSlotId);
        long currentSlotValue = redisTemplate.opsForValue().increment(currentKey, 1);
        if (currentSlotValue == 1) {
            redisTemplate.expire(currentKey, EXPIRATION_IN_MINUTES);
        }
        return currentSlotValue + previousCarryOver <= MAX_REQUESTS_ALLOWED_PER_10_MINUTES;
    }

    @Override
    public RateLimitingAlgorithm getAlgorithm() {
        return RateLimitingAlgorithm.SLIDING_WINDOW_COUNTER;
    }
}
