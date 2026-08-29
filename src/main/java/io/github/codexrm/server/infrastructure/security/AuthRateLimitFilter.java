package io.github.codexrm.server.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.codexrm.server.api.dto.response.ErrorResponse;
import io.github.codexrm.server.infrastructure.config.RateLimitConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;


public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthRateLimitFilter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private final RateLimitConfig config;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(RateLimitConfig config) {
        this.config = config;
    }

    private boolean appliesTo(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/auth/signin") || path.equals("/api/auth/refresh-token");
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(
                config.getCapacity(),
                Refill.intervally(config.getRefillTokens(), Duration.ofMinutes(config.getRefillMinutes()))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (!appliesTo(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        logger.warn("event=authorization.rate_limited path={} ip={}", request.getRequestURI(), key);

        response.setContentType("application/json");
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                "Too many attempts. Please try again later.",
                request.getRequestURI()
        );

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}