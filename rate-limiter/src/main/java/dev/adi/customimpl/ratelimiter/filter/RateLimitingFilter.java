package dev.adi.customimpl.ratelimiter.filter;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import dev.adi.customimpl.ratelimiter.RateLimiter;
import dev.adi.customimpl.ratelimiter.strategy.RateLimitingStrategy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {
    private final RateLimitingStrategy strategy;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
            try {
                RateLimiter rateLimiter = strategy.getStrategy(request.getParameter("type"));
                if (rateLimiter == null) {
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                    return;
                }
                if (!rateLimiter.isAllowed(request.getRemoteAddr())) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    return;
                }
                filterChain.doFilter(request, response);
            } catch (Exception exception) {
                log.error("Error while doing rate liming {} ", exception.getMessage());
                response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            }
    }
}
