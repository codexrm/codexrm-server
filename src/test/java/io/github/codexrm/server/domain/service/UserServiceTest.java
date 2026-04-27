package io.github.codexrm.server.domain.service;

import io.github.codexrm.server.component.DTOConverter;
import io.github.codexrm.server.api.dto.UserDTO;
import io.github.codexrm.server.infrastructure.exception.InvalidOperationException;
import io.github.codexrm.server.domain.model.Role;
import io.github.codexrm.server.domain.model.User;
import io.github.codexrm.server.api.dto.request.AddUserRequest;
import io.github.codexrm.server.api.dto.request.SignupRequest;
import io.github.codexrm.server.api.dto.request.UpdateUserPasswordRequest;
import io.github.codexrm.server.infrastructure.persistence.repository.UserRepository;
import io.github.codexrm.server.domain.service.RoleService;
import io.github.codexrm.server.domain.service.UserService;
import io.github.codexrm.server.infrastructure.exception.ResourceNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private RoleService roleService;

    @Mock
    private DTOConverter dtoConverter;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // TEST: get()
    @Test
    void shouldReturnUser_whenUserExists() {
        User user = new User();
        user.setId(1);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        User result = userService.get(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void shouldThrowException_whenUserNotExists() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.get(1));
    }

    // TEST: getAll()
    @Test
    void shouldReturnPage_whenUsernameIsNull() {
        Page<User> page = new PageImpl<>(java.util.List.of(new User()));

        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<User> result = userService.getAll(null, 0, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void shouldReturnFilteredPage_whenUsernameProvided() {
        Page<User> page = new PageImpl<>(java.util.List.of(new User()));

        when(userRepository.findByUsernameContaining(eq("john"), any(Pageable.class)))
                .thenReturn(page);

        Page<User> result = userService.getAll("john", 0, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    // TEST: add()
    @Test
    void shouldSaveUser() {
        User user = new User();

        when(userRepository.save(user)).thenReturn(user);

        User result = userService.add(user);

        assertNotNull(result);
        verify(userRepository).save(user);
    }

    // TEST: update()
    @Test
    void shouldUpdateUser_whenExists() {
        User user = new User();
        user.setId(1);

        when(userRepository.existsById(1)).thenReturn(true);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.update(user);

        assertNotNull(result);
    }

    @Test
    void shouldThrowException_whenUpdatingNonExistingUser() {
        User user = new User();
        user.setId(1);

        when(userRepository.existsById(1)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.update(user));
    }

    // TEST: delete()
    @Test
    void shouldDeleteUser_whenExists() {
        User user = new User();
        user.setId(1);

        when(userRepository.existsById(1)).thenReturn(true);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        userService.delete(1);

        verify(userRepository).delete(user);
    }

    @Test
    void shouldThrowException_whenDeletingNonExistingUser() {
        when(userRepository.existsById(1)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.delete(1));
    }

    // TEST: validateUser()
    @Test
    void shouldThrowException_whenUsernameExists() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                userService.validateUniqueUser("john", "email@test.com"));
    }

    @Test
    void shouldThrowException_whenEmailExists() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("email@test.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                userService.validateUniqueUser("john", "email@test.com"));
    }

    @Test
    void shouldPassValidation_whenUserIsValid() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("email@test.com")).thenReturn(false);

        assertDoesNotThrow(() ->
                userService.validateUniqueUser("john", "email@test.com"));
    }

    //TEST: createUserAccount()
    @Test
    void shouldCreateUserAccountWithDefaultRole() {
        User user = new User();
        Set<Role> roles = Set.of(new Role());

        when(roleService.resolveRoles(List.of("ROLE_USER"))).thenReturn(roles);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.createUserAccount(user, true, null);

        assertNotNull(result);
        assertEquals(roles, user.getRoles());

        verify(roleService).resolveRoles(List.of("ROLE_USER"));
        verify(userRepository).save(user);
    }

    @Test
    void shouldCreateUserAccountWithProvidedRoles() {
        User user = new User();
        Set<Role> roles = Set.of(new Role());

        when(roleService.resolveRoles(List.of("ROLE_ADMIN"))).thenReturn(roles);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.createUserAccount(user, false, List.of("ROLE_ADMIN"));

        assertNotNull(result);
        assertEquals(roles, user.getRoles());

        verify(roleService).resolveRoles(List.of("ROLE_ADMIN"));
        verify(userRepository).save(user);
    }

    // TEST: createUser()
    @Test
    void shouldCreateUser() {
        AddUserRequest request = mock(AddUserRequest.class);

        when(request.getUsername()).thenReturn("john");
        when(request.getName()).thenReturn("John");
        when(request.getLastName()).thenReturn("Doe");
        when(request.getEmail()).thenReturn("john@test.com");
        when(request.isEnabled()).thenReturn(true);
        when(request.getPassword()).thenReturn("123");
        when(request.getRoles()).thenReturn(List.of("ROLE_ADMIN"));

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);

        when(passwordEncoder.encode("123")).thenReturn("ENCODED");

        when(roleService.resolveRoles(List.of("ROLE_ADMIN")))
                .thenReturn(Set.of(new Role()));

        when(userRepository.save(any(User.class)))
                .thenReturn(new User());

        User result = userService.createUser(request);

        assertNotNull(result);
    }

    // TEST: registerUser()
    @Test
    void shouldRegisterUser() {
        SignupRequest request = mock(SignupRequest.class);

        when(request.getUsername()).thenReturn("john");
        when(request.getName()).thenReturn("John");
        when(request.getLastName()).thenReturn("Doe");
        when(request.getEmail()).thenReturn("john@test.com");
        when(request.isEnabled()).thenReturn(true);
        when(request.getPassword()).thenReturn("123");

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);

        when(passwordEncoder.encode("123")).thenReturn("ENCODED");

        when(roleService.resolveRoles(List.of("ROLE_USER")))
                .thenReturn(Set.of(new Role()));

        when(userRepository.save(any(User.class)))
                .thenReturn(new User());

        User result = userService.registerUser(request);

        assertNotNull(result);
    }

    // TEST: updatePassword()
    @Test
    void shouldUpdatePassword() {
        User user = new User();
        user.setId(1);
        user.setPassword("OLD");

        UpdateUserPasswordRequest request = mock(UpdateUserPasswordRequest.class);

        when(request.getCurrentPassword()).thenReturn("123");
        when(request.getNewPassword()).thenReturn("456");
        when(request.getConfirmationPassword()).thenReturn("456");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123", "OLD")).thenReturn(true);
        when(passwordEncoder.encode("456")).thenReturn("NEW");
        when(userRepository.save(user)).thenReturn(user);

        assertDoesNotThrow(() ->
                userService.updatePassword(1, request));

        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowWhenCurrentPasswordIncorrect() {
        User user = new User();
        user.setId(1);
        user.setPassword("OLD");

        UpdateUserPasswordRequest request = mock(UpdateUserPasswordRequest.class);

        when(request.getCurrentPassword()).thenReturn("wrong");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "OLD")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> userService.updatePassword(1, request));
    }

    @Test
    void shouldThrowWhenPasswordsDoNotMatch() {
        User user = new User();
        user.setId(1);
        user.setPassword("OLD");

        UpdateUserPasswordRequest request = mock(UpdateUserPasswordRequest.class);

        when(request.getCurrentPassword()).thenReturn("123");
        when(request.getNewPassword()).thenReturn("456");
        when(request.getConfirmationPassword()).thenReturn("999");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123", "OLD")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.updatePassword(1, request));
    }

    // TEST: updatePreferences()
    @Test
    void shouldUpdatePreferences() {
        UserDTO dto = new UserDTO();
        dto.setId(1);

        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setPassword("PASS");
        existingUser.setRoles(Set.of(new Role()));

        User converted = new User();
        converted.setId(1);

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(dtoConverter.toUser(dto)).thenReturn(converted);
        when(userRepository.save(converted)).thenReturn(converted);

        User result = userService.updatePreferences(1, dto);

        assertNotNull(result);
        assertEquals("PASS", converted.getPassword());
    }

    @Test
    void shouldThrowWhenUpdatingAnotherUserPreferences() {
        UserDTO dto = new UserDTO();
        dto.setId(2);

        assertThrows(InvalidOperationException.class,
                () -> userService.updatePreferences(1, dto));
    }
}