package io.github.codexrm.server.infrastructure.persistence.repository;

import io.github.codexrm.server.domain.model.Reference;
import io.github.codexrm.server.domain.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ReferenceRepositoryTest {

    @Autowired
    private ReferenceRepository referenceRepository;

    @Autowired
    private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager entityManager;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("john");
        user.setEmail("john@test.com");
        user.setPassword("123");
        user.setName("John");
        user.setLastName("Doe");
        user.setEnabled(true);

        entityManager.persist(user);
        entityManager.flush();
    }

    @Test
    void shouldFindByUser() {
        Reference ref = new Reference();
        ref.setUser(user);
        ref.setTitle("Test title");
        ref.setYear("2024");

        entityManager.persist(ref);

        Page<Reference> result = referenceRepository.findByUser(user, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldFilterByTitle() {
        Reference ref = new Reference();
        ref.setUser(user);
        ref.setTitle("Spring Boot Guide");
        ref.setYear("2024");

        entityManager.persist(ref);

        Page<Reference> result = referenceRepository
                .findByUserAndTitleContaining(user, "Spring", PageRequest.of(0, 10));

        assertFalse(result.isEmpty());
    }

    @Test
    void shouldFilterByYear() {
        Reference ref = new Reference();
        ref.setUser(user);
        ref.setTitle("Any");
        ref.setYear("2023");

        entityManager.persist(ref);

        Page<Reference> result = referenceRepository
                .findByUserAndYearContaining(user, "2023", PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }

    @Test
    void shouldFilterByYearAndTitle() {
        Reference ref = new Reference();
        ref.setUser(user);
        ref.setTitle("Java");
        ref.setYear("2022");

        entityManager.persist(ref);

        Page<Reference> result = referenceRepository.findByUserAndYearContainingAndTitleContaining(user, "2022", "Java", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldReturnEmptyPage() {
        Page<Reference> result = referenceRepository
                .findByUser(user, PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldPaginateReferences() {
        for (int i = 0; i < 15; i++) {
            Reference ref = new Reference();
            ref.setUser(user);
            ref.setTitle("Ref " + i);
            ref.setYear("2024");
            entityManager.persist(ref);
        }

        entityManager.flush();

        Page<Reference> page1 = referenceRepository.findByUser(user, PageRequest.of(0, 10));
        Page<Reference> page2 = referenceRepository.findByUser(user, PageRequest.of(1, 10));

        assertEquals(10, page1.getContent().size());
        assertEquals(5, page2.getContent().size());
    }

    @Test
    void shouldNotReturnReferencesFromOtherUsers() {
        User otherUser = new User();
        otherUser.setUsername("other");
        otherUser.setEmail("other@test.com");
        otherUser.setPassword("123");
        otherUser.setName("Other");
        otherUser.setLastName("User");
        otherUser.setEnabled(true);

        entityManager.persist(otherUser);

        Reference ref = new Reference();
        ref.setUser(otherUser);
        ref.setTitle("Other ref");
        ref.setYear("2024");

        entityManager.persist(ref);
        entityManager.flush();

        Page<Reference> result = referenceRepository.findByUser(user, PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenTitleDoesNotMatch() {
        Reference ref = new Reference();
        ref.setUser(user);
        ref.setTitle("Java");
        ref.setYear("2024");

        entityManager.persist(ref);
        entityManager.flush();

        Page<Reference> result = referenceRepository
                .findByUserAndTitleContaining(user, "Python", PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFilterIgnoringCase() {
        Reference ref = new Reference();
        ref.setUser(user);
        ref.setTitle("Spring Boot");
        ref.setYear("2024");

        entityManager.persist(ref);
        entityManager.flush();

        Page<Reference> result = referenceRepository
                .findByUserAndTitleContainingIgnoreCase(user, "spring", PageRequest.of(0, 10));

        assertFalse(result.isEmpty());
    }
}