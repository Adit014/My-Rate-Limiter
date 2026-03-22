package dev.adi.customimpl.ratelimiter.enums;

import java.util.Arrays;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RateLimitingAlgorithm {
    FIXED_WINDOW("fixed_window"),
    LEAKY_BUCKET("leaky_bucket"),
    SLIDING_WINDOW_COUNTER("sliding_window_counter"),
    SLIDING_WINDOW_LOG("sliding_window_log"),
    TOKEN_BUCKET("token_bucket")
    ;

    private final String queryParam;

    private static final List<RateLimitingAlgorithm> values = Arrays.asList(values());

    public static RateLimitingAlgorithm getAlgorithm(String value) {
        return values.stream().filter(algo -> algo.queryParam.equals(value)).findFirst().orElse(null);
    }
}
