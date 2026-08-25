package io.github.codexrm.server.infrastructure.config;

import io.github.codexrm.server.infrastructure.logging.CorrelationIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class LoggingConfig {

    // Registered with the highest possible precedence so the
    // correlationId exists in MDC before ANY other filter runs —
    // including the entire Spring Security filter chain. This is what
    // lets GlobalExceptionHandler, AuthEntryPointJwt, and
    // AccessDeniedHandlerImpl all have access to it, since none of
    // those run inside the normal controller flow.
    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistration(
            CorrelationIdFilter filter) {

        FilterRegistrationBean<CorrelationIdFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}