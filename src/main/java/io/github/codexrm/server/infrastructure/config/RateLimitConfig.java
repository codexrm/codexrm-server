package io.github.codexrm.server.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "codexrm.ratelimit.auth")
public class RateLimitConfig {

    private int capacity = 5;
    private int refillTokens = 5;
    private int refillMinutes = 1;

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getRefillTokens() {
        return refillTokens;
    }

    public void setRefillTokens(int refillTokens) {
        this.refillTokens = refillTokens;
    }

    public int getRefillMinutes() {
        return refillMinutes;
    }

    public void setRefillMinutes(int refillMinutes) {
        this.refillMinutes = refillMinutes;
    }
}