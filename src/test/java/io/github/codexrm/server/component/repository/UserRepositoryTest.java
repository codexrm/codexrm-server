package io.github.codexrm.server.component.repository;

import io.github.codexrm.server.model.User;
import io.github.codexrm.server.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("123");
        user.setEmail(email);
        user.setName("Test");
        user.setLastName("User");
        user.setEnabled(true);
        return user;
    }

    @Test
    void shouldSaveAndFindUser() {
        User user = createUser("testuser", "test@test.com");

        User saved = userRepository.save(user);

        Optional<User> result = userRepository.findById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void shouldFindByUsername() {
        User user = createUser("maria", "maria@test.com");
        userRepository.save(user);

        Optional<User> result = userRepository.findByUsername("maria");

        assertTrue(result.isPresent());
        assertEquals("maria", result.get().getUsername());
    }

    @Test
    void shouldReturnEmptyWhenUsernameNotFound() {
        Optional<User> result = userRepository.findByUsername("no-existe");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCheckIfUsernameExists() {
        userRepository.save(createUser("pedro", "pedro@test.com"));

        Boolean exists = userRepository.existsByUsername("pedro");

        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseIfUsernameDoesNotExist() {
        Boolean exists = userRepository.existsByUsername("ghost");

        assertFalse(exists);
    }

    @Test
    void shouldCheckIfEmailExists() {
        userRepository.save(createUser("ana", "ana@test.com"));

        Boolean exists = userRepository.existsByEmail("ana@test.com");

        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseIfEmailDoesNotExist() {
        Boolean exists = userRepository.existsByEmail("no@test.com");

        assertFalse(exists);
    }

    @Test
    void shouldFindUsersByUsernameContaining() {
        userRepository.save(createUser("juan123", "juan@test.com"));
        userRepository.save(createUser("juan456", "juan2@test.com"));
        userRepository.save(createUser("pedro", "pedro@test.com"));

        Page<User> result = userRepository.findByUsernameContaining("juan", PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void shouldReturnPagedUsers() {
        userRepository.save(createUser("user1", "u1@test.com"));
        userRepository.save(createUser("user2", "u2@test.com"));
        userRepository.save(createUser("user3", "u3@test.com"));

        Page<User> page = userRepository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
    }

    @Test
    void shouldReturnEmptyPageWhenNoMatch() {
        userRepository.save(createUser("carlos", "c@test.com"));

        Page<User> result = userRepository.findByUsernameContaining("zzz", PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindUsersIgnoringCase() {
        userRepository.save(createUser("Maria", "m@test.com"));

        Page<User> result = userRepository
                .findByUsernameContainingIgnoreCase("maria", PageRequest.of(0, 10));

        assertFalse(result.isEmpty());
    }
}