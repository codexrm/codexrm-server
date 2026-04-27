package io.github.codexrm.server.domain.service;

import io.github.codexrm.server.infrastructure.exception.DuplicateResourceException;
import io.github.codexrm.server.infrastructure.exception.ResourceNotFoundException;
import io.github.codexrm.server.domain.model.Reference;
import io.github.codexrm.server.domain.model.User;
import io.github.codexrm.server.infrastructure.persistence.repository.ReferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReferenceServiceTest {

    @Mock
    private ReferenceRepository referenceRepository;

    @InjectMocks
    private ReferenceService referenceService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1);
    }

    // TEST: get()
    @Test
    void shouldReturnReference_whenExists() {
        Reference ref = new Reference();
        ref.setId(1);

        when(referenceRepository.findById(1)).thenReturn(Optional.of(ref));

        Reference result = referenceService.get(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void shouldThrowException_whenReferenceNotFound() {
        when(referenceRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> referenceService.get(1));
    }

    // TEST: getAll()
    @Test
    void shouldReturnAll_whenNoFilters() {
        Page<Reference> page = new PageImpl<>(List.of(new Reference()));

        when(referenceRepository.findByUser(eq(user), any(Pageable.class))).thenReturn(page);

        Page<Reference> result = referenceService.getAll(user, null, null, 0, 10, null);

        assertFalse(result.isEmpty());
    }

    @Test
    void shouldFilterByTitle() {
        Page<Reference> page = new PageImpl<>(List.of(new Reference()));

        when(referenceRepository.findByUserAndTitleContaining(eq(user), eq("test"), any(Pageable.class)))
                .thenReturn(page);

        Page<Reference> result = referenceService.getAll(user, null, "test", 0, 10, null);

        assertFalse(result.isEmpty());
    }

    @Test
    void shouldFilterByYear() {
        Page<Reference> page = new PageImpl<>(List.of(new Reference()));

        when(referenceRepository.findByUserAndYearContaining(eq(user), eq("2024"), any(Pageable.class)))
                .thenReturn(page);

        Page<Reference> result = referenceService.getAll(user, "2024", null, 0, 10, null);

        assertFalse(result.isEmpty());
    }

    @Test
    void shouldFilterByYearAndTitle() {
        Page<Reference> page = new PageImpl<>(List.of(new Reference()));

        when(referenceRepository.findByUserAndYearContainingAndTitleContaining(
                eq(user), eq("2024"), eq("test"), any(Pageable.class)))
                .thenReturn(page);

        Page<Reference> result = referenceService.getAll(user, "2024", "test", 0, 10, null);

        assertFalse(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyPage() {
        when(referenceRepository.findByUser(eq(user), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<Reference> result = referenceService.getAll(user, null, null, 0, 10, null);

        assertTrue(result.isEmpty());
    }

    // TEST: add()
    @Test
    void shouldSaveReference() {
        Reference ref = new Reference();

        when(referenceRepository.save(ref)).thenReturn(ref);

        Reference result = referenceService.add(ref);

        assertNotNull(result);
        verify(referenceRepository).save(ref);
    }

    @Test
    void shouldThrowException_whenReferenceAlreadyExists() {
        Reference ref = new Reference();
        ref.setId(1);

        when(referenceRepository.existsById(1)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> referenceService.add(ref));
    }

    // TEST: update()
    @Test
    void shouldUpdateReference_whenExists() {
        Reference ref = new Reference();
        ref.setId(1);

        when(referenceRepository.existsById(1)).thenReturn(true);
        when(referenceRepository.findById(1)).thenReturn(Optional.of(ref));
        when(referenceRepository.save(ref)).thenReturn(ref);

        Reference result = referenceService.update(ref);

        assertNotNull(result);
    }

    @Test
    void shouldThrowException_whenUpdatingNonExistingReference() {
        Reference ref = new Reference();
        ref.setId(1);

        when(referenceRepository.existsById(1)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> referenceService.update(ref));
    }

    // TEST: delete()
    @Test
    void shouldDeleteReference_whenExists() {
        Reference ref = new Reference();
        ref.setId(1);

        when(referenceRepository.existsById(1)).thenReturn(true);
        when(referenceRepository.findById(1)).thenReturn(Optional.of(ref));

        referenceService.delete(1);

        verify(referenceRepository).delete(ref);
    }

    @Test
    void shouldThrowException_whenDeletingNonExistingReference() {
        when(referenceRepository.existsById(1)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> referenceService.delete(1));
    }
}