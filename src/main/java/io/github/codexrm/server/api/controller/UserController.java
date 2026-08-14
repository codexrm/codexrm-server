package io.github.codexrm.server.api.controller;

import io.github.codexrm.server.component.DTOConverter;
import io.github.codexrm.server.api.dto.PageDTO;
import io.github.codexrm.server.api.dto.UserDTO;
import io.github.codexrm.server.api.dto.UserPageDTO;
import io.github.codexrm.server.domain.enums.SortUser;
import io.github.codexrm.server.domain.model.User;
import io.github.codexrm.server.api.dto.request.AddUserRequest;
import io.github.codexrm.server.api.dto.request.UpdateUserPasswordRequest;
import io.github.codexrm.server.api.dto.response.ErrorResponse;
import io.github.codexrm.server.api.dto.response.MessageResponse;
import io.github.codexrm.server.infrastructure.security.services.UserDetailsImpl;
import io.github.codexrm.server.domain.service.UserService;
import io.github.codexrm.server.infrastructure.exception.InvalidOperationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management operations")
public class UserController {

    private final UserService userService;
    private final DTOConverter dtoConverter;

    @Autowired
    public UserController(UserService userService, DTOConverter dtoConverter) {
        this.userService = userService;
        this.dtoConverter = dtoConverter;
    }

    @Operation(summary = "Get paginated users")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserPageDTO> getAll(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "sort", required = false) String sort) {

        SortUser sortUser = sort != null ? SortUser.fromValue(sort) : null;

        Page<User> users = userService.getAll(username, page, size, sortUser);

        List<UserDTO> userDTOList = dtoConverter.toUserDTOList(users.getContent());
        PageDTO pageDTO = new PageDTO(users.getNumber(), users.getTotalElements(), users.getTotalPages());

        return ResponseEntity.ok(new UserPageDTO(userDTOList, pageDTO));
    }

    @Operation(summary = "Get user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('AUDITOR')")
    public ResponseEntity<UserDTO> getById(
            @Parameter(description = "User ID", required = true)
            @PathVariable("id") Integer id) {

        UserDetailsImpl userDetails =
                (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (userDetails.getId().equals(id) || isAdmin(userDetails)) {
            return ResponseEntity.ok(dtoConverter.toUserDTO(userService.get(id)));
        }

        throw new InvalidOperationException("You do not have permission to access this resource");
    }

    @Operation(summary = "Create a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Duplicate user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> add(@Valid @RequestBody AddUserRequest request) {

        userService.createUser(request);
        return ResponseEntity.ok(new MessageResponse("User created successfully"));
    }

    @Operation(summary = "Update a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully updated"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> update(@PathVariable ("id") Integer id,
                                          @Valid @RequestBody UserDTO userDTO) {

        User user = dtoConverter.toUser(userDTO);
        user.setId(id);
        user.setPassword(userService.getPasswordById(id));
        return ResponseEntity.ok(dtoConverter.toUserDTO(userService.update(user)));
    }

    @Operation(summary = "Update user password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid password",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
    @PutMapping("/password")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdateUserPasswordRequest request) {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.updatePassword(userDetails.getId(), request);
        return ResponseEntity.ok(new MessageResponse("Password updated"));
    }

    @Operation(summary = "Update user preferences")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preferences successfully updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    @PutMapping("/preferences")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserDTO> updatePreferences(@Valid @RequestBody UserDTO userDTO) {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User updatedUser = userService.updatePreferences(userDetails.getId(), userDTO);
        return ResponseEntity.ok(dtoConverter.toUserDTO(updatedUser));
    }

    @Operation(summary = "Delete a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully deleted"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(
            @Parameter(description = "User ID", required = true)
            @PathVariable("id") Integer id){

        userService.delete(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete multiple users")
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteGroup(@Valid @RequestBody List<Integer> ids) {
        ids.forEach(userService::delete);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get user details")
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getMe() {

        UserDetailsImpl userDetails =
                (UserDetailsImpl) SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

        User user = userService.get(userDetails.getId());

        return ResponseEntity.ok(dtoConverter.toUserDTO(user));
    }

    private boolean isAdmin(UserDetailsImpl userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(io.github.codexrm.server.domain.enums.ERole.ROLE_ADMIN.name()));
    }
}