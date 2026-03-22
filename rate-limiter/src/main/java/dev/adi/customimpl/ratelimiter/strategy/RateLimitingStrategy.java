package dev.adi.customimpl.ratelimiter.strategy;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.adi.customimpl.ratelimiter.RateLimiter;
import dev.adi.customimpl.ratelimiter.enums.RateLimitingAlgorithm;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RateLimitingStrategy {
    private List<RateLimiter> rateLimiters;

    public RateLimiter getStrategy(String queryParam) {
        RateLimitingAlgorithm algorithm = RateLimitingAlgorithm.getAlgorithm(queryParam);
        if (algorithm == null) return null;
        return rateLimiters.stream().filter(limiter ->algorithm == limiter.getAlgorithm()).findFirst().orElse(null);
    }
}
