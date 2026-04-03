package service;

import io.github.codexrm.server.model.User;
import io.github.codexrm.server.repository.RoleRepository;
import io.github.codexrm.server.repository.UserRepository;
import io.github.codexrm.server.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.data.domain.*;

import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // =========================
    // TEST: get()
    // =========================

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

        assertThrows(EntityNotFoundException.class, () -> userService.get(1));
    }

    // =========================
    // TEST: getAll()
    // =========================

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

    // =========================
    // TEST: add()
    // =========================

    @Test
    void shouldSaveUser() {
        User user = new User();

        when(userRepository.save(user)).thenReturn(user);

        User result = userService.add(user);

        assertNotNull(result);
        verify(userRepository).save(user);
    }

    // =========================
    // TEST: update()
    // =========================

    @Test
    void shouldUpdateUser_whenExists() {
        User user = new User();
        user.setId(1);

        when(userRepository.existsById(1)).thenReturn(true);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.update(user);

        assertNotNull(result);
    }

    @Test
    void shouldThrowException_whenUpdatingNonExistingUser() {
        User user = new User();
        user.setId(1);

        when(userRepository.existsById(1)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> userService.update(user));
    }

    // =========================
    // TEST: delete()
    // =========================

    @Test
    void shouldDeleteUser_whenExists() {
        when(userRepository.existsById(1)).thenReturn(true);

        userService.delete(1);

        verify(userRepository).deleteById(1);
    }

    @Test
    void shouldThrowException_whenDeletingNonExistingUser() {
        when(userRepository.existsById(1)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> userService.delete(1));
    }

    // =========================
    // TEST: validateUser()
    // =========================

    @Test
    void shouldThrowException_whenUsernameExists() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                userService.validateUser("john", "email@test.com"));
    }

    @Test
    void shouldThrowException_whenEmailExists() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("email@test.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                userService.validateUser("john", "email@test.com"));
    }

    @Test
    void shouldPassValidation_whenUserIsValid() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("email@test.com")).thenReturn(false);

        assertDoesNotThrow(() ->
                userService.validateUser("john", "email@test.com"));
    }
}