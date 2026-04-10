package io.github.codexrm.server.component.service;

import io.github.codexrm.server.enums.ERole;
import io.github.codexrm.server.exception.ResourceNotFoundException;
import io.github.codexrm.server.model.Role;
import io.github.codexrm.server.repository.RoleRepository;
import io.github.codexrm.server.service.RoleService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
    }

    @Test
    void shouldResolveValidRoles() {

        Role adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);

        when(roleRepository.findByName(ERole.ROLE_ADMIN))
                .thenReturn(Optional.of(adminRole));

        Set<Role> result = roleService.resolveRoles(List.of("ROLE_ADMIN"));

        assertEquals(1, result.size());
        assertTrue(result.contains(adminRole));

        verify(roleRepository).findByName(ERole.ROLE_ADMIN);
    }

    @Test
    void shouldReturnDefaultRoleWhenNull() {

        when(roleRepository.findByName(ERole.ROLE_USER))
                .thenReturn(Optional.of(userRole));

        Set<Role> result = roleService.resolveRoles(null);

        assertEquals(1, result.size());
        assertTrue(result.contains(userRole));

        verify(roleRepository).findByName(ERole.ROLE_USER);
    }

    @Test
    void shouldReturnDefaultRoleWhenEmpty() {

        when(roleRepository.findByName(ERole.ROLE_USER))
                .thenReturn(Optional.of(userRole));

        Set<Role> result = roleService.resolveRoles(List.of());

        assertEquals(1, result.size());
        assertTrue(result.contains(userRole));
    }

    @Test
    void shouldThrowExceptionForInvalidRole() {

        List<String> roles = List.of("INVALID_ROLE");

        assertThrows(ResourceNotFoundException.class,
                () -> roleService.resolveRoles(roles));

        verify(roleRepository, never()).findByName(any());
    }

    @Test
    void shouldThrowExceptionWhenRoleNotFoundInDatabase() {

        when(roleRepository.findByName(ERole.ROLE_ADMIN))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roleService.resolveRoles(List.of("ROLE_ADMIN")));

        verify(roleRepository).findByName(ERole.ROLE_ADMIN);
    }

}