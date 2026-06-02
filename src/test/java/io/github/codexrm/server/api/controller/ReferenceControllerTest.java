package io.github.codexrm.server.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.codexrm.server.component.DTOConverter;
import io.github.codexrm.server.api.dto.ReferenceDTO;
import io.github.codexrm.server.api.dto.ReferenceLibraryDTO;
import io.github.codexrm.server.domain.model.Reference;
import io.github.codexrm.server.domain.model.User;
import io.github.codexrm.server.infrastructure.security.services.UserDetailsImpl;
import io.github.codexrm.server.domain.service.ReferenceService;
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

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReferenceController.class)
@Import({DTOConverter.class, ReferenceControllerTest.TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = true)
class ReferenceControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReferenceService referenceService;

    @MockBean
    private UserService userService;

    @MockBean
    private DTOConverter dtoConverter;

    @Autowired
    private ObjectMapper objectMapper;

    // USERS
    // Mock de usuario autenticado (ROLE_USER)
    private UserDetailsImpl mockUser() {
        return new UserDetailsImpl(
                1,
                "user",
                "Name",
                "Last",
                "user@test.com",
                true,
                "password",
                List.of(() -> "ROLE_USER")
        );
    }

    // Mock de entidad User
    private User mockUserEntity() {
        User user = new User();
        user.setId(1);
        return user;
    }

    //GET ALL
    //  Debe retornar lista paginada de referencias (200)
    @Test
    void shouldGetAllReferences() throws Exception {

        User user = mockUserEntity();
        Page<Reference> page = new PageImpl<>(List.of(new Reference()));

        when(userService.get(1)).thenReturn(user);
        when(referenceService.getAll(any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(page);

        when(dtoConverter.toReferenceDTOList(any()))
                .thenReturn(List.of(new ReferenceDTO()));

        mockMvc.perform(get("/api/references")
                        .with(user(mockUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referenceDTOList").exists())
                .andExpect(jsonPath("$.pageDTO").exists());
    }

    //GET BY ID
    // Debe retornar una referencia por ID (200)
    @Test
    void shouldGetReferenceById() throws Exception {

        User user = mockUserEntity();
        Reference reference = new Reference();

        when(userService.get(1)).thenReturn(user);
        when(referenceService.get(1)).thenReturn(reference);
        doNothing().when(referenceService).validateOwnership(anyInt(), any());

        when(dtoConverter.toReferenceDTO(any()))
                .thenReturn(new ReferenceDTO());

        mockMvc.perform(get("/api/references/1")
                        .with(user(mockUser())))
                .andExpect(status().isOk());
    }

    // CREATE
    // Debe crear una referencia correctamente (201)
    @Test
    void shouldCreateReference() throws Exception {

        User user = mockUserEntity();
        Reference reference = new Reference();

        when(userService.get(1)).thenReturn(user);
        when(dtoConverter.createReference(any(), any())).thenReturn(reference);
        when(referenceService.add(any())).thenReturn(reference);
        when(dtoConverter.toReferenceDTO(any())).thenReturn(new ReferenceDTO());

        mockMvc.perform(post("/api/references")
                        .with(user(mockUser()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReferenceDTO())))
                .andExpect(status().isCreated());
    }

    //DELETE
    // Debe eliminar una referencia por ID (200)
    @Test
    void shouldDeleteReference() throws Exception {

        User user = mockUserEntity();
        Reference reference = new Reference();

        when(userService.get(1)).thenReturn(user);
        when(referenceService.get(1)).thenReturn(reference);
        doNothing().when(referenceService).validateOwnership(anyInt(), any());

        mockMvc.perform(delete("/api/references/1")
                        .with(user(mockUser()))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(referenceService).delete(1);
    }

    //DELETE GROUP
    // Debe eliminar múltiples referencias (200)
    @Test
    void shouldDeleteGroupReferences() throws Exception {

        User user = mockUserEntity();
        Reference ref = new Reference();

        when(userService.get(1)).thenReturn(user);
        when(referenceService.get(anyInt())).thenReturn(ref);
        doNothing().when(referenceService).validateOwnership(anyInt(), any());

        List<Integer> ids = List.of(1, 2);

        mockMvc.perform(delete("/api/references")
                        .with(user(mockUser()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk());

        verify(referenceService, times(2)).delete(anyInt());
    }

    // SYNC
    // Debe sincronizar referencias correctamente (200)
    @Test
    void shouldSyncReferences() throws Exception {

        User user = mockUserEntity();

        ReferenceLibraryDTO library = new ReferenceLibraryDTO();
        library.setNewReferencesList(List.of());
        library.setUpdatedReferencesList(List.of());
        library.setDeletedReferencesList(List.of());
        library.setSortReference(null);

        Page<Reference> page = new PageImpl<>(List.of(new Reference()));

        when(userService.get(1)).thenReturn(user);
        when(dtoConverter.toReferenceList(any(), any())).thenReturn(List.of());
        doNothing().when(referenceService).sync(eq(user), any(), any(), any());
        when(referenceService.getAll(any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(page);

        when(dtoConverter.toReferenceDTOList(any()))
                .thenReturn(List.of(new ReferenceDTO()));

        mockMvc.perform(post("/api/references/sync")
                        .with(user(mockUser()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(library)))
                .andExpect(status().isOk());
    }

    // Debe retornar 204 cuando no hay contenido tras sync
    @Test
    void shouldReturnNoContentAfterSync() throws Exception {

        User user = mockUserEntity();

        ReferenceLibraryDTO library = new ReferenceLibraryDTO();
        library.setNewReferencesList(List.of());
        library.setUpdatedReferencesList(List.of());
        library.setDeletedReferencesList(List.of());
        library.setSortReference(null);

        when(userService.get(1)).thenReturn(user);
        when(dtoConverter.toReferenceList(any(), any())).thenReturn(List.of());
        doNothing().when(referenceService).sync(eq(user), any(), any(), any());
        when(referenceService.getAll(any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(post("/api/references/sync")
                        .with(user(mockUser()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(library)))
                .andExpect(status().isNoContent());
    }

    // IMPORT
    //  Debe importar referencias desde archivo (200)
    @Test
    void shouldImportReferences() throws Exception {

        User user = mockUserEntity();

        MockMultipartFile file = new MockMultipartFile(
                "uploadFile",
                "test.ris",
                "text/plain",
                "content".getBytes()
        );

        when(userService.get(1)).thenReturn(user);
        when(referenceService.importReferences(anyString(), anyString()))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(multipart("/api/references/import")
                        .file(file)
                        .param("format", "RIS")
                        .with(user(mockUser()))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // Debe fallar si el archivo está vacío (400)
    @Test
    void shouldFailImportWhenFileIsEmpty() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "uploadFile",
                "empty.ris",
                "text/plain",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/references/import")
                        .file(file)
                        .param("format", "RIS")
                        .with(user(mockUser()))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // EXPORT
    // Debe exportar referencias correctamente (200)
    @Test
    void shouldExportReferences() throws Exception {

        User user = mockUserEntity();
        Reference ref = new Reference();

        when(userService.get(1)).thenReturn(user);
        when(referenceService.get(anyInt())).thenReturn(ref);

        doAnswer(invocation -> {
            File file = invocation.getArgument(0);
            Files.createDirectories(file.toPath().getParent());
            Files.write(file.toPath(), "test".getBytes());
            return null;
        }).when(referenceService).exportReferences(any(), any(), any());

        List<Integer> ids = List.of(1);

        mockMvc.perform(post("/api/references/export")
                        .with(user(mockUser()))
                        .with(csrf())
                        .param("fileName", "file.txt")
                        .param("format", "RIS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk());
    }

    //ALL USERS
    //  Debe permitir acceso a MANAGER (200)
    @Test
    void shouldGetAllFromUsersAsManager() throws Exception {

        Page<Reference> page = new PageImpl<>(List.of(new Reference()));

        when(referenceService.getAllFromUsers(any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(page);

        when(dtoConverter.toReferenceDTOList(any()))
                .thenReturn(List.of(new ReferenceDTO()));

        mockMvc.perform(get("/api/references/all-users")
                        .with(user("admin").roles("MANAGER")))
                .andExpect(status().isOk());
    }

    // SECURITY
    // Debe retornar 401 si no está autenticado
    @Test
    void shouldReturnUnauthorizedWhenNoAuth() throws Exception {

        mockMvc.perform(get("/api/references"))
                .andExpect(status().isUnauthorized());
    }
}