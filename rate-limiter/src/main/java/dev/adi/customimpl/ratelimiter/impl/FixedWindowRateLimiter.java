package dev.adi.customimpl.ratelimiter.impl;

import static dev.adi.customimpl.ratelimiter.enums.RateLimitingAlgorithm.FIXED_WINDOW;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import dev.adi.customimpl.ratelimiter.RateLimiter;
import dev.adi.customimpl.ratelimiter.enums.RateLimitingAlgorithm;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FixedWindowRateLimiter implements RateLimiter {
    private static final String FIXED_TOKEN_KEY_FORMAT = "ratelimit:fixed:%s:%d";
    private static final Duration EXPIRATION_IN_MINUTES = Duration.ofMinutes(10);
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean isAllowed(String key) {
        // getting slot id - use current time - start of day and find the minutes
        // difference and since we are going to do 10 minutes slot find id per 10 minute
        // and use in the key.
        long slotId = getSlotId();
        String redisKey = String.format(FIXED_TOKEN_KEY_FORMAT, key, slotId);
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count == 1) {
            redisTemplate.expire(redisKey, EXPIRATION_IN_MINUTES);
        }
        return count <= MAX_REQUESTS_ALLOWED_PER_10_MINUTES;
    }

    private long getSlotId() {
        Instant currentTime = Instant.now();
        LocalDate today = LocalDate.now(ZoneOffset.UTC); // Today's date in the default time zone
        ZonedDateTime startOfDay = today.atStartOfDay(ZoneOffset.UTC); // Midnight as ZonedDateTime
        Instant startInstant = startOfDay.toInstant(); // Convert to Instant for comparison
        return Duration.between(startInstant, currentTime).toMinutes() / 10;
    }

    @Override
    public RateLimitingAlgorithm getAlgorithm() {
        return FIXED_WINDOW;
    }

}
