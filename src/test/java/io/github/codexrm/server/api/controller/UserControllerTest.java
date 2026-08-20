package io.github.codexrm.server.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.codexrm.server.component.DTOConverter;
import io.github.codexrm.server.api.dto.UserDTO;
import io.github.codexrm.server.domain.model.User;
import io.github.codexrm.server.api.dto.request.AddUserRequest;
import io.github.codexrm.server.infrastructure.security.services.UserDetailsImpl;
import io.github.codexrm.server.domain.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({DTOConverter.class, UserControllerTest.TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = true)
class UserControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private DTOConverter dtoConverter;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDetailsImpl mockAdmin() {
        return new UserDetailsImpl(
                1,
                "admin",
                "Admin",
                "Admin",
                "admin@test.com",
                true,
                "password",
                List.of(() -> "ROLE_ADMIN")
        );
    }

    private UserDetailsImpl mockUser() {
        return new UserDetailsImpl(
                2,
                "user",
                "User",
                "User",
                "user@test.com",
                true,
                "password",
                List.of(() -> "ROLE_USER")
        );
    }

    private UserDetailsImpl mockAuditor() {
        return new UserDetailsImpl(
                3,
                "auditor",
                "Auditor",
                "Auditor",
                "auditor@test.com",
                true,
                "password",
                List.of(() -> "ROLE_AUDITOR")
        );
    }


    // GET /api/users
    @Test
    void shouldGetAllUsers() throws Exception {
        User user = new User();
        user.setId(1);
        user.setUsername("john");

        Page<User> page = new PageImpl<>(List.of(user));

        when(userService.getAll(any(), anyInt(), anyInt(), any()))
                .thenReturn(page);

        when(dtoConverter.toUserDTOList(any()))
                .thenReturn(List.of(new UserDTO()));

        mockMvc.perform(get("/api/users")
                        .with(user(mockAdmin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userDTOList").exists())
                .andExpect(jsonPath("$.pageDTO").exists());
    }

    // SIN AUTH → 401
    @Test
    void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    // GET BY ID (ADMIN)
    @Test
    void shouldGetUserByIdAsAdmin() throws Exception {
        User user = new User();
        user.setId(1);

        when(userService.get(1)).thenReturn(user);
        when(dtoConverter.toUserDTO(any())).thenReturn(new UserDTO());

        mockMvc.perform(get("/api/users/1")
                        .with(user(mockAdmin())))
                .andExpect(status().isOk());
    }

    // GET BY ID (AUDITOR puede ver cualquier usuario)
    @Test
    void shouldGetUserByIdAsAuditor() throws Exception {
        User user = new User();
        user.setId(1);

        when(userService.get(1)).thenReturn(user);
        when(dtoConverter.toUserDTO(any())).thenReturn(new UserDTO());

        mockMvc.perform(get("/api/users/1")
                        .with(user(mockAuditor())))
                .andExpect(status().isOk());
    }

    // GET BY ID (USER normal NO puede ver a otro usuario)
    @Test
    void shouldForbidUserFromGettingAnotherUsersById() throws Exception {
        mockMvc.perform(get("/api/users/999")
                        .with(user(mockUser())))
                .andExpect(status().isForbidden());
    }




    // CREATE USER (ADMIN)
    @Test
    void shouldCreateUser() throws Exception {
        AddUserRequest request = new AddUserRequest();
        request.setUsername("newuser");
        request.setPassword("Password123");
        request.setEmail("test@test.com");
        request.setName("John");
        request.setLastName("Doe");

        mockMvc.perform(post("/api/users")
                        .with(user(mockAdmin()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User created successfully"));

        verify(userService).createUser(any());
    }

    // CREATE USER SIN ADMIN → 403
    @Test
    void shouldReturnForbiddenWhenNotAdminCreatingUser() throws Exception {
        AddUserRequest request = new AddUserRequest();
        request.setUsername("user");
        request.setPassword("Password123");
        request.setEmail("test@test.com");
        request.setName("User");
        request.setLastName("Test");

        mockMvc.perform(post("/api/users")
                        .with(user(mockUser()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // DELETE USER (ADMIN)
    @Test
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .with(user(mockAdmin()))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(userService).delete(1);
    }

    // FILTER + PAGINATION
    @Test
    void shouldFilterUsersByUsername() throws Exception {
        when(userService.getAll(eq("john"), anyInt(), anyInt(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        when(dtoConverter.toUserDTOList(any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/users")
                        .param("username", "john")
                        .with(user(mockAdmin())))
                .andExpect(status().isOk());

        verify(userService).getAll(eq("john"), anyInt(), anyInt(), any());
    }
}