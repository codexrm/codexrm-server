# springdoc-openapi - Documentation & API Design Analysis

## 1. Overview

springdoc-openapi is a library that integrates OpenAPI 3 with Spring Boot applications. It automatically generates API documentation and provides Swagger UI support for REST endpoints.

This document summarizes key findings about springdoc-openapi configuration, DTO inheritance documentation, Swagger UI customization, and OpenAPI annotations relevant to the current project architecture.

---

## 2. DTO Inheritance Documentation

springdoc-openapi supports inheritance hierarchies in DTOs, but explicit configuration is often required for accurate schema generation.

Example:

```java
class ReferenceDTO {
    String title;
}

class ArticleReferenceDTO extends ReferenceDTO {
    String author;
}
```

To improve schema documentation for inherited DTOs, annotations such as `@Schema` can be used:

```java
@Schema(description = "Base reference DTO used for different types of bibliographic references")
class ReferenceDTO {
    String title;
}

@Schema(description = "Article reference DTO")
class ArticleReferenceDTO extends ReferenceDTO {
    String author;
}
```

Key observations:

- DTO inheritance is reflected in generated OpenAPI schemas
- Explicit annotations improve Swagger documentation clarity
- Complex inheritance hierarchies may require manual schema customization

---

## 3. SecurityScheme Configuration

springdoc-openapi integrates with Spring Security and supports JWT/Bearer authentication documentation through OpenAPI annotations.

Example:

```java
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
```

This configuration allows Swagger UI to expose an authorization button for authenticated requests.

Common benefits:

- centralized authentication documentation
- easier API testing through Swagger UI
- consistent security definitions across endpoints

Security requirements can also be applied at endpoint level:

```java
@SecurityRequirement(name = "bearerAuth")
```

Key observations:

- JWT authentication integrates cleanly with Swagger UI
- SecurityScheme configuration improves API usability
- Explicit security documentation reduces onboarding complexity

---

## 4. Swagger UI Customization

springdoc-openapi allows Swagger UI customization through application properties and OpenAPI configuration classes.

Common customizations include:

- custom API title and description
- custom Swagger UI path
- endpoint grouping
- sorting operations and tags
- hiding internal endpoints

Example configuration:

```properties
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
```

Example OpenAPI configuration:

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
            .info(new Info()
            .title("CodexRM API")
            .description("Reference Manager API")
            .version("1.0.0"));
}
```
Key observations:

- Swagger UI customization improves developer experience
- Organized documentation simplifies endpoint navigation
- Custom API metadata provides clearer project documentation

---

## 5. OpenAPI Examples and Annotations

springdoc-openapi supports annotations for documenting endpoints, request bodies, responses, and examples.

Common annotations:

- `@Operation`
- `@ApiResponse`
- `@Schema`
- `@Parameter`
- `@ExampleObject`

Example:

```java
@Operation(summary = "Create a new reference")
@ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Reference successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid request",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
@PostMapping
@PreAuthorize("hasRole('USER')")
public ReferenceDTO createReference(@RequestBody CreateReferenceRequest request) {
    return service.create(request);
}
```

Example using `@Schema`:

```java
@Schema(description = "Title of the reference", example = "Deep Learning")
private String title;
```

Key observations:

- Explicit annotations improve API readability
- Examples help consumers understand request structures
- Well-documented endpoints reduce integration issues

---

## 6. Conclusion

springdoc-openapi provides strong integration with Spring Boot and simplifies API documentation generation.

The library works well for:

- documenting REST APIs
- exposing Swagger UI
- configuring JWT authentication
- documenting DTO inheritance structures

However, inheritance-heavy DTO models may still require explicit annotations and manual schema adjustments for better clarity.

For the current project architecture, springdoc-openapi can improve maintainability, onboarding, and API consistency when combined with well-structured DTOConverter and controller layers.