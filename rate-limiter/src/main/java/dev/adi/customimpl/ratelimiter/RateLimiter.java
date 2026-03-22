package dev.adi.customimpl.ratelimiter;

import dev.adi.customimpl.ratelimiter.enums.RateLimitingAlgorithm;

public interface RateLimiter {
    public static int MAX_REQUESTS_ALLOWED_PER_10_MINUTES = 100;
    boolean isAllowed(String key);
    
    RateLimitingAlgorithm getAlgorithm();
}
