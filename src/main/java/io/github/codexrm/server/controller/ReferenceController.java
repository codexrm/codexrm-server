package io.github.codexrm.server.controller;

import io.github.codexrm.server.component.DTOConverter;
import io.github.codexrm.server.dto.PageDTO;
import io.github.codexrm.server.dto.ReferenceDTO;
import io.github.codexrm.server.dto.ReferenceLibraryDTO;
import io.github.codexrm.server.dto.ReferencePageDTO;
import io.github.codexrm.server.enums.SortReference;
import io.github.codexrm.server.model.Reference;
import io.github.codexrm.server.model.User;
import io.github.codexrm.server.security.services.UserDetailsImpl;
import io.github.codexrm.server.service.ReferenceService;
import io.github.codexrm.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jbibtex.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RequestMapping("/api/Reference")
@RestController
@Tag(name = "References", description = "Operations related to user references")
public class ReferenceController {

    private static final String UPLOADED_FOLDER = "/app/tempUpload";

    private final ReferenceService referenceService;
    private final UserService userService;
    private final DTOConverter dtoConverter;

    @Autowired
    public ReferenceController(ReferenceService referenceService, UserService userService, DTOConverter dtoConverter) {
        this.referenceService = referenceService;
        this.userService = userService;
        this.dtoConverter = dtoConverter;
    }

    // ===================== HELPERS =====================

    private User getAuthenticatedUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userService.get(userDetails.getId());
    }

    private boolean isOwner(Integer userId, Integer referenceUserId) {
        return Objects.equals(referenceUserId, userId);
    }

    private ArrayList<Integer> filterUserReferences(Integer userId, ArrayList<Integer> referenceIds) {
        ArrayList<Integer> filtered = new ArrayList<>();

        for (Integer id : referenceIds) {
            Reference reference = referenceService.get(id);
            if (isOwner(userId, reference.getUser().getId())) {
                filtered.add(id);
            }
        }
        return filtered;
    }

    private ReferencePageDTO buildPageDTO(Page<Reference> page) {
        if (page.getContent().isEmpty()) return null;

        List<ReferenceDTO> dtoList = dtoConverter.toReferenceDTOList(page.getContent());
        PageDTO pageDTO = new PageDTO(page.getNumber(), page.getTotalElements(), page.getTotalPages());

        return new ReferencePageDTO(dtoList, pageDTO);
    }

    // ===================== ENDPOINTS =====================

    @Operation(summary = "Get paginated references of the authenticated user")
    @PostMapping("/GetAll")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReferencePageDTO> getAll(
            @RequestParam(name = "year", required = false) String year,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "id,desc", required = false) String sort) {

        SortReference sortReference = null;
        if (sort != null) {
            sortReference = SortReference.fromValue(sort);
        }

        User user = getAuthenticatedUser();
        Page<Reference> result = referenceService.getAll(user, year, title, page, size, sortReference);

        if (result.getContent().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return ResponseEntity.ok(buildPageDTO(result));
    }

    @Operation(summary = "Get a reference by ID")
    @GetMapping("/Get/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReferenceDTO> getById(
                    @Parameter(description = "ID de la referencia", required = true)
                    @PathVariable("id") Integer id)  {

        User user = getAuthenticatedUser();
        Reference reference = referenceService.get(id);

        if (!isOwner(user.getId(), reference.getUser().getId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(dtoConverter.toReferenceDTO(reference));
    }

    @Operation(summary = "Create a new reference")
    @PostMapping("/Add")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReferenceDTO> add(@Valid @RequestBody ReferenceDTO referenceDTO) {

        User user = getAuthenticatedUser();
        Reference reference = dtoConverter.createReference(referenceDTO, user);

        Reference saved = referenceService.add(reference);
        return new ResponseEntity<>(dtoConverter.toReferenceDTO(saved), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing reference")
    @PutMapping("/Update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReferenceDTO> update(@Valid @RequestBody ReferenceDTO referenceDTO) {

        User user = getAuthenticatedUser();
        Reference reference = dtoConverter.toReference(referenceDTO, user);

        Reference updated = referenceService.update(reference);
        return ResponseEntity.ok(dtoConverter.toReferenceDTO(updated));
    }

    @Operation(summary = "Delete a reference")
    @DeleteMapping("/Delete/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> delete(
            @Parameter(description = "ID de la referencia", required = true)
            @PathVariable("id") Integer id) {

        User user = getAuthenticatedUser();
        Reference reference = referenceService.get(id);

        if (!isOwner(user.getId(), reference.getUser().getId())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        referenceService.delete(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete multiple references")
    @PostMapping("/DeleteGroup")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteGroup(@Valid @RequestBody ArrayList<Integer> idList) {

        User user = getAuthenticatedUser();
        ArrayList<Integer> filtered = filterUserReferences(user.getId(), idList);

        filtered.forEach(referenceService::delete);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Sync references")
    @PostMapping("/Sync")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReferencePageDTO> sync(
            @RequestParam(name = "author", required = false) String author,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @Valid @RequestBody ReferenceLibraryDTO library) {

        User user = getAuthenticatedUser();

        List<Reference> newList = dtoConverter.toReferenceList(library.getNewReferencesList(), user);
        List<Reference> updateList = dtoConverter.toReferenceList(library.getUpdatedReferencesList(), user);

        referenceService.sync(newList, updateList, library.getDeletedReferencesList());

        Page<Reference> result = referenceService.getAll(user, author, title, page, size, library.getSortReference());

        if (result.getContent().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return ResponseEntity.ok(buildPageDTO(result));
    }

    @Operation(summary = "Import references")
    @PostMapping(value = "/Import", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> importReferences(
            @RequestParam (name = "format", defaultValue = "RIS") String format,
            @RequestParam (name = "uploadFile") MultipartFile uploadFile) {

        if (uploadFile.isEmpty()) {
            return ResponseEntity.badRequest().body("You must select a file!");
        }

        try {
            saveUploadedFile(uploadFile);

            User user = getAuthenticatedUser();
            File file = new File(UPLOADED_FOLDER, uploadFile.getOriginalFilename());

            ArrayList<Reference> list = referenceService.importReferences(file.getPath(), format);

            for (Reference r : list) {
                r.setUser(user);
                referenceService.add(r);
            }

            file.delete();

            return ResponseEntity.ok("Reference Imported!");

        } catch (IOException | ParseException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Export references")
    @PostMapping("/Export")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Resource> exportReferences(
            @RequestParam (name = "fileName", defaultValue = "file.txt") String fileName,
            @RequestParam (name = "format", defaultValue = "RIS") String format,
            @Valid @RequestBody ArrayList<Integer> idList) throws IOException {

        User user = getAuthenticatedUser();
        ArrayList<Integer> filteredIds = filterUserReferences(user.getId(), idList);

        ArrayList<Reference> references = new ArrayList<>();
        for (Integer id : filteredIds) {
            references.add(referenceService.get(id));
        }

        Path path = Paths.get(UPLOADED_FOLDER, fileName);
        Files.createDirectories(path.getParent());

        File file = new File(path.toString());
        referenceService.exportReferences(file, references, format);

        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

        file.delete();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @Operation(summary = "Get references from all users")
    @PostMapping("/GetAllFromUsers")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ReferencePageDTO> getAllFromUsers(
            @RequestParam(name = "year", required = false) String year,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @Valid @RequestBody(required = false) SortReference sort) {

        Page<Reference> result = referenceService.getAllFromUsers(year, title, page, size, sort);

        if (result.getContent().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return ResponseEntity.ok(buildPageDTO(result));
    }

    // ===================== FILE HANDLING =====================

    private void saveUploadedFile(MultipartFile file) throws IOException {
        String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("upload.tmp");

        fileName = Paths.get(fileName).getFileName().toString();

        Path path = Paths.get(UPLOADED_FOLDER, fileName);
        Files.createDirectories(path.getParent());
        Files.write(path, file.getBytes());
    }
}