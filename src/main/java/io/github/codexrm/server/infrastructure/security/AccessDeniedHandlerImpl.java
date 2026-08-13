package io.github.codexrm.server.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.codexrm.server.api.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(AccessDeniedHandlerImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        logger.warn("Access denied for {}: {}", request.getRequestURI(), accessDeniedException.getMessage());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "You do not have permission to access this resource",
                request.getRequestURI()
        );

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}