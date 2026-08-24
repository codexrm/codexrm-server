package io.github.codexrm.server.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.Collections;
import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@OpenAPIDefinition(
        security = @SecurityRequirement(name = "bearerAuth")
)
public class OpenApiConfig {

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "registerUser",
            "authenticateUser",
            "refreshtoken"
    );

    @Bean
    public OpenAPI codexrmOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("CodexRM API")
                .description("Reference Manager API")
                .version("1.0.0"));
    }

    // Explicitly clears the security requirement for public auth endpoints,
    // overriding the global bearerAuth requirement declared above.
    @Bean
    public OperationCustomizer publicEndpointsCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            String methodName = handlerMethod.getMethod().getName();
            if (PUBLIC_ENDPOINTS.contains(methodName)) {
                operation.setSecurity(Collections.emptyList());
            }
            return operation;
        };
    }
}