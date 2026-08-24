package io.github.codexrm.server.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.codexrm.server.domain.model.RefreshToken;
import io.github.codexrm.server.domain.model.User;
import io.github.codexrm.server.api.dto.request.LoginRequest;
import io.github.codexrm.server.api.dto.request.SignupRequest;
import io.github.codexrm.server.api.dto.request.TokenRefreshRequest;
import io.github.codexrm.server.infrastructure.security.jwt.JwtUtils;
import io.github.codexrm.server.infrastructure.security.services.RefreshTokenService;
import io.github.codexrm.server.infrastructure.security.services.UserDetailsImpl;
import io.github.codexrm.server.domain.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = true)
class AuthControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtils jwtUtils;

    // SIGNUP
    @Test
    void shouldRegisterUser() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setUsername("test");
        request.setPassword("123456");
        request.setEmail("test@test.com");
        request.setName("Name");
        request.setLastName("Last");

        mockMvc.perform(post("/api/auth/signup")
                        .with(user("test"))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        verify(userService).registerUser(any());
    }

    // SIGNIN
    @Test
    void shouldAuthenticateUser() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("test");
        request.setPassword("123456");

        UserDetailsImpl userDetails = new UserDetailsImpl(
                1,
                "test",
                "Name",
                "Last",
                "test@test.com",
                true,
                "password",
                List.of(() -> "ROLE_USER")
        );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        refreshToken.setExpiryDate(Instant.now().plusSeconds(3600));

        when(refreshTokenService.createRefreshToken(anyInt())).thenReturn(refreshToken);

        when(jwtUtils.generateJwtToken(any())).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/signin")
                        .with(user("test"))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    // REFRESH TOKEN
    @Test
    void shouldRefreshToken() throws Exception {
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("refresh-token");

        User user = new User();
        user.setUsername("test");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(3600));

        RefreshToken rotatedToken = new RefreshToken();
        rotatedToken.setToken("rotated-refresh-token");
        rotatedToken.setUser(user);
        rotatedToken.setExpiryDate(Instant.now().plusSeconds(3600));

        when(refreshTokenService.findByToken(anyString()))
                .thenReturn(Optional.of(refreshToken));

        when(refreshTokenService.verifyExpiration(any()))
                .thenReturn(refreshToken);

        when(refreshTokenService.rotateRefreshToken(any()))
                .thenReturn(rotatedToken);

        when(jwtUtils.generateTokenFromUsername(any()))
                .thenReturn("new-jwt");

        when(jwtUtils.getJwtExpirationMs()).thenReturn(1000);

        mockMvc.perform(post("/api/auth/refresh-token")
                        .with(user("test"))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-jwt"))
                .andExpect(jsonPath("$.refreshToken").value("rotated-refresh-token"));
    }

    // LOGOUT
    @Test
    void shouldLogoutUser() throws Exception {

        UserDetailsImpl userDetails = new UserDetailsImpl(
                1,
                "test",
                "Name",
                "Last",
                "test@test.com",
                true,
                "password",
                List.of(() -> "ROLE_USER")
        );

        mockMvc.perform(post("/api/auth/logout")
                        .with(user(userDetails))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(refreshTokenService).deleteByUserId(1);
    }

    // LOGOUT SIN AUTH → 401
    @Test
    void shouldReturnUnauthorizedOnLogoutWhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}