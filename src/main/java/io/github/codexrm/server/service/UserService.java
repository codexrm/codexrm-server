package io.github.codexrm.server.service;

import io.github.codexrm.server.component.DTOConverter;
import io.github.codexrm.server.dto.UserDTO;
import io.github.codexrm.server.enums.SortUser;
import io.github.codexrm.server.exception.DuplicateResourceException;
import io.github.codexrm.server.exception.InvalidOperationException;
import io.github.codexrm.server.exception.ResourceNotFoundException;
import io.github.codexrm.server.model.Role;
import io.github.codexrm.server.model.User;
import io.github.codexrm.server.payload.request.AddUserRequest;
import io.github.codexrm.server.payload.request.SignupRequest;
import io.github.codexrm.server.payload.request.UpdateUserPasswordRequest;
import io.github.codexrm.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final DTOConverter dtoConverter;

    @Autowired
    public UserService(final UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder, DTOConverter dtoConverter) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.dtoConverter = dtoConverter;
    }

    public Page<User> getAll(String username, int page, int size, SortUser sort) {

        Sort.Order order = getOrder(sort);
        Pageable pagingSort = PageRequest.of(page, size, Sort.by(order));

        if (username == null)
            return userRepository.findAll(pagingSort);
        else
            return userRepository.findByUsernameContaining(username, pagingSort);
    }

    public User get(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    public String getPasswordById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return user.getPassword();
    }

    public User add(User user) {return userRepository.save(user);}

    public User createUser(AddUserRequest request) {

        validateUniqueUser(request.getUsername(), request.getEmail());

        User user = new User(
                request.getUsername(),
                request.getName(),
                request.getLastName(),
                request.getEmail(),
                request.isEnabled(),
                passwordEncoder.encode(request.getPassword())
        );

        return createUserAccount(user, false, request.getRoles());
    }

    public User createUserAccount(User user, boolean isUser, List<String> roleList) {

        Set<Role> roles;

        if (isUser) {
            roles = roleService.resolveRoles(List.of("ROLE_USER"));
        } else {
            roles = roleService.resolveRoles(roleList);
        }

        user.setRoles(roles);
        return add(user);
    }

    public User registerUser(SignupRequest request) {

        validateUniqueUser(request.getUsername(), request.getEmail());

        User user = new User(
                request.getUsername(),
                request.getName(),
                request.getLastName(),
                request.getEmail(),
                request.isEnabled(),
                passwordEncoder.encode(request.getPassword())
        );

        return createUserAccount(user, true, null);
    }

    public User update(User user) {

        if (user.getId() == null) {
            throw new InvalidOperationException("User ID must not be null for update");
        }

        get(user.getId());

        return userRepository.save(user);
    }

    public void updatePassword(Integer userId, UpdateUserPasswordRequest request) {

        User user = get(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        update(user);
    }

    public User updatePreferences(Integer authenticatedUserId, UserDTO userDTO) {

        if (!authenticatedUserId.equals(userDTO.getId())) {
            throw new InvalidOperationException("You can only update your own preferences");
        }

        User existingUser = get(userDTO.getId());

        User user = dtoConverter.toUser(userDTO);

        user.setRoles(existingUser.getRoles());
        user.setPassword(existingUser.getPassword());

        return update(user);
    }

    public void delete(Integer id) {
        User user = get(id);
        userRepository.delete(user);
    }

    public void validateUniqueUser(String username, String email) {

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("User", "username", username);
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }
    }

    private Sort.Order getOrder(SortUser sort) {

        if (sort == null) {
            return new Sort.Order(Sort.Direction.ASC, "id");
        }

        return switch (sort) {
            case idAsc -> new Sort.Order(Sort.Direction.ASC, "id");
            case idDesc -> new Sort.Order(Sort.Direction.DESC, "id");
            case nameAsc -> new Sort.Order(Sort.Direction.ASC, "name");
            case nameDesc -> new Sort.Order(Sort.Direction.DESC, "name");
            case lastNameAsc -> new Sort.Order(Sort.Direction.ASC, "lastname");
            case lastNameDesc -> new Sort.Order(Sort.Direction.DESC, "lastname");
            case emailAsc -> new Sort.Order(Sort.Direction.ASC, "email");
            case emailDesc -> new Sort.Order(Sort.Direction.DESC, "email");
            case enabledAsc -> new Sort.Order(Sort.Direction.ASC, "enabled");
            case enabledDesc -> new Sort.Order(Sort.Direction.DESC, "enabled");
            case usernameAsc -> new Sort.Order(Sort.Direction.ASC, "username");
            case usernameDesc -> new Sort.Order(Sort.Direction.DESC, "username");
            case passwordAsc -> new Sort.Order(Sort.Direction.ASC, "password");
            default -> new Sort.Order(Sort.Direction.DESC, "password");
        };
    }
}