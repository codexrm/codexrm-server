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
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RequestMapping("/api/Reference")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
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

    @Operation(summary = "Get paginated references of the authenticated user with optional filters")
    @PostMapping("/GetAll")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReferencePageDTO> getAll(

            @Parameter(description = "Filter references by publication year", example = "2024")
            @RequestParam( name = "year", required = false) String year,

            @Parameter(description = "Filter references by title", example = "Machine Learning")
            @RequestParam(name = "title", required = false) String title,

            @Parameter(description = "Page number", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size,

            @Parameter(description = "Sorting options for reference")
            @RequestBody(required = false) SortReference sort) {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.get(userDetails.getId());

        Page<Reference> pageTuts = referenceService.getAll(user, year, title, page, size, sort);

        if (pageTuts == null)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

         else {
            ReferencePageDTO referencePageDTO = getReferencePageDTO(pageTuts);
            return ResponseEntity.ok().body(referencePageDTO);
        }
    }

    @Operation(summary = "Get a reference by ID")
    @GetMapping("/Get/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReferenceDTO> getById(
            @Parameter(
                    name = "id",
                    description = "Reference ID",
                    example = "1",
                    required = true,
                    in = ParameterIn.PATH )
            @PathVariable("id") final Integer id){

    UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Reference reference = referenceService.get(id);

        if (verificateUser(userDetails.getId(), reference.getUser().getId())) {
            ReferenceDTO referenceDTO = dtoConverter.toReferenceDTO(reference);
            return ResponseEntity.ok().body(referenceDTO);

        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @Operation(summary = "Create a new reference")
    @PostMapping("/Add")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReferenceDTO> add(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Reference information to create",
                    required = true)
            @RequestBody final ReferenceDTO referenceDTO) {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Reference reference = dtoConverter.createReference(referenceDTO, userService.get(userDetails.getId()));

        if (reference != null) {
            ReferenceDTO referenceDTOAdded = dtoConverter.toReferenceDTO(referenceService.add(reference));
            return new ResponseEntity<>(referenceDTOAdded, HttpStatus.CREATED);

        } else return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
    }

    @Operation(summary = "Update an existing reference")
    @PutMapping("/Update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReferenceDTO> update(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Reference data to update",
                    required = true)
            @RequestBody final ReferenceDTO referenceDTO) {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Reference reference = dtoConverter.toReference(referenceDTO, userService.get(userDetails.getId()));

        if (reference != null) {
            ReferenceDTO referenceDTOUpdated = dtoConverter.toReferenceDTO(referenceService.update(reference));
            return new ResponseEntity<>(referenceDTOUpdated, HttpStatus.OK);

        } else return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }

    @Operation(summary = "Delete a reference by ID")
    @DeleteMapping("/Delete/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> delete(
            @Parameter(
                    name = "id",
                    description = "Reference ID",
                    example = "1",
                    required = true,
                    in = ParameterIn.PATH )
            @PathVariable("id") final Integer id){

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Reference reference = referenceService.get(id);

        if (verificateUser(userDetails.getId(), reference.getUser().getId())) {
            referenceService.delete(id);
            return ResponseEntity.ok().build();

        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @Operation(summary = "Delete multiple references by list of IDs")
    @PostMapping("/DeleteGroup")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteGroup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of reference IDs to delete",
                    required = true)
            @RequestBody ArrayList<Integer> idList) {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ArrayList<Integer> newList = verificateUser(userDetails.getId(), idList);

        for (Integer id : newList) {
            referenceService.delete(id);
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Import references from a file")
    @PostMapping("/Sync")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReferencePageDTO> sync(
            @Parameter(description = "Filter by author", example = "Smith")
            @RequestParam(name = "author", required = false) String author,

            @Parameter(description = "Filter by title", example = "Artificial Intelligence")
            @RequestParam(name = "title", required = false) String title,

            @Parameter(description = "Page number", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Reference library synchronization data",
                    required = true)
            @RequestBody final ReferenceLibraryDTO referenceLibrary) {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.get(userDetails.getId());

        List<Reference> newReferenceList = dtoConverter.toReferenceList(referenceLibrary.getNewReferencesList(), user);
        List<Reference> updateReferenceList = dtoConverter.toReferenceList(referenceLibrary.getUpdatedReferencesList(), user);

        referenceService.sync(newReferenceList, updateReferenceList, referenceLibrary.getDeletedReferencesList());

        ReferencePageDTO referencePageDTO = getReferencePageDTO(referenceService.getAll(user, author, title, page, size, referenceLibrary.getSortReference()));

        if (referencePageDTO == null)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        else return ResponseEntity.ok().body(referencePageDTO);
    }

    @Operation(summary = "Import references from a file")
    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "/Import", consumes = {"multipart/form-data"})
    public ResponseEntity<?> importReferences(

            @Parameter(description = "Import format", example = "RIS OR BIBTEX")
            @RequestParam("format") String format,

            @Parameter(description = "File to upload")
            @RequestParam("uploadFile") MultipartFile uploadFile) {

        if (uploadFile.isEmpty())
            return new ResponseEntity<>("You must select a file!", HttpStatus.OK);

        try {
            saveUploadedFiles(List.of(uploadFile));

            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = userService.get(userDetails.getId());

            File file = new File(UPLOADED_FOLDER, uploadFile.getOriginalFilename());

            ArrayList<Reference> refereceList = referenceService.importReferences(file.getPath(), format);
            for (Reference reference : refereceList) {
                reference.setUser(user);
                referenceService.add(reference);
            }
            file.delete();
            return new ResponseEntity<>("Reference Imported!", HttpStatus.OK);

        } catch (IOException | ParseException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }
    @Operation(summary = "Export references to a file")
    @RequestMapping(path = "/Export", method = RequestMethod.POST)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Resource> exportReferences(

            @Parameter(description = "Name of the exported file", example = "references.txt")
            @RequestParam("fileName") String fileName,

            @Parameter(description = "Export format", example = "RIS OR BIBTEX")
            @RequestParam("format") String format,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of reference IDs to export",
                    required = true)
            @RequestBody final ArrayList<Integer> idList) throws IOException {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ArrayList<Reference> referenceList = new ArrayList<>();

        ArrayList<Integer> newIdList = verificateUser(userDetails.getId(), idList);
        for (Integer id : newIdList) {
            referenceList.add(referenceService.get(id));
        }

        Path path = Paths.get(UPLOADED_FOLDER, fileName);
        Files.createDirectories(path.getParent());
        File file = new File(path.toString());

        try {
            referenceService.exportReferences(file, referenceList, format);
        } catch (IOException e) {
            throw new RuntimeException("Error exporting file", e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @Operation(summary = "Get references from all users (manager only)")
    @PostMapping("/GetAllFromUsers")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ReferencePageDTO> getAllFromUsers(

            @Parameter(description = "Filter references by publication year", example = "2024")
            @RequestParam( name = "year", required = false) String year,

            @Parameter(description = "Filter references by title", example = "Machine Learning")
            @RequestParam(name = "title", required = false) String title,

            @Parameter(description = "Page number", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size,

            @Parameter(description = "Sorting options for reference")
            @RequestBody(required = false) SortReference sort) {

        Page<Reference> pageTuts = referenceService.getAllFromUsers(year, title, page, size, sort);

        if (pageTuts == null)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        else {
            ReferencePageDTO referencePageDTO = getReferencePageDTO(pageTuts);
            return ResponseEntity.ok().body(referencePageDTO);
        }
    }

    private ReferencePageDTO getReferencePageDTO(Page<Reference> pageTuts) {
        if (pageTuts.getContent().isEmpty())
            return null;

        List<ReferenceDTO> referenceDTOList = dtoConverter.toReferenceDTOList(pageTuts.getContent());
        PageDTO pageDTO = new PageDTO(pageTuts.getNumber(), pageTuts.getTotalElements(), pageTuts.getTotalPages());

        return new ReferencePageDTO(referenceDTOList, pageDTO);
    }

    // save file
    private void saveUploadedFiles(List<MultipartFile> files) throws IOException {

        for (MultipartFile file : files) {
            if (file.isEmpty())
                continue;

            String fileName = file.getOriginalFilename();

            if (fileName == null || fileName.isBlank()) {
                fileName = "upload.tmp";
            }

            fileName = Paths.get(fileName).getFileName().toString();
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOADED_FOLDER, fileName);

            Files.createDirectories(path.getParent());
            Files.write(path, bytes);
        }
    }

    private boolean verificateUser(Integer userId, Integer referenceUserId) {
        return Objects.equals(referenceUserId, userId);
    }

    private ArrayList<Integer> verificateUser(Integer userId, ArrayList<Integer> referenceId) {
        ArrayList<Integer> idList = new ArrayList<>();

        for (Integer id : referenceId) {
            Reference reference = referenceService.get(id);
            if (verificateUser(userId, reference.getUser().getId()))
                idList.add(id);
        }
        return idList;
    }
}
