package io.github.codexrm.server.service;

import io.github.codexrm.server.enums.ERole;
import io.github.codexrm.server.enums.SortUser;
import io.github.codexrm.server.model.Role;
import io.github.codexrm.server.model.User;
import io.github.codexrm.server.repository.RoleRepository;
import io.github.codexrm.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserService(final UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    public String getPasswordById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return user.getPassword();
    }

    public User add(User user) {
        return userRepository.save(user);
    }

    public User update(User user) {
        if (user.getId() != null && !userRepository.existsById(user.getId())) {
            throw new EntityNotFoundException("User not found with id: " + user.getId());
        }
        return userRepository.save(user);
    }

    public void delete(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public void validateUser(String username, String email) {

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Error: Email is already in use!");
        }
    }

    public User createUserAccount(User user, boolean isUser, List<String> roleList) {

        Set<Role> roles = new HashSet<>();

        if (isUser) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new EntityNotFoundException("Role ROLE_USER not found"));
            roles.add(userRole);

        } else {
            if (roleList == null) {
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new EntityNotFoundException("Role ROLE_USER not found"));
                roles.add(userRole);

            } else {
                roleList.forEach(role -> {
                    switch (role) {
                        case "ROLE_ADMIN":
                            roles.add(roleRepository.findByName(ERole.ROLE_ADMIN)
                                    .orElseThrow(() -> new EntityNotFoundException("Role ROLE_ADMIN not found")));
                            break;

                        case "ROLE_MANAGER":
                            roles.add(roleRepository.findByName(ERole.ROLE_MANAGER)
                                    .orElseThrow(() -> new EntityNotFoundException("Role ROLE_MANAGER not found")));
                            break;

                        case "ROLE_AUDITOR":
                            roles.add(roleRepository.findByName(ERole.ROLE_AUDITOR)
                                    .orElseThrow(() -> new EntityNotFoundException("Role ROLE_AUDITOR not found")));
                            break;

                        default:
                            roles.add(roleRepository.findByName(ERole.ROLE_USER)
                                    .orElseThrow(() -> new EntityNotFoundException("Role ROLE_USER not found")));
                    }
                });
            }
        }

        user.setRoles(roles);
        return add(user);
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