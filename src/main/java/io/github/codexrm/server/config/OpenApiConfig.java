package io.github.codexrm.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI codexrmOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CodexRM API")
                        .description("Reference Manager API")
                        .version("1.0.0"));
    }
}