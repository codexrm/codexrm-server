package io.github.codexrm.server.service;

import io.github.codexrm.server.enums.ERole;
import io.github.codexrm.server.enums.SortUser;
import io.github.codexrm.server.model.Role;
import io.github.codexrm.server.model.User;
import io.github.codexrm.server.repository.RoleRepository;
import io.github.codexrm.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        return userRepository.findById(id).get();
    }

    public String getPasswordById(Integer id) {
        User user = userRepository.findById(id).get();
        return user.getPassword();
    }

    public User add(User user) {
        return userRepository.save(user);
    }

    public User update(User user) {
        if (user.getId() != null && !userRepository.existsById(user.getId()))
            throw new EntityNotFoundException("There is no entity with such ID in the database.");

        return userRepository.save(user);
    }

    public void delete(Integer id) {
        userRepository.deleteById(id);
    }

    public String validateUser(String username, String email) {
        String exist = "Valid";
        if (userRepository.existsByUsername(username))
            exist = "Error: Username is already taken!";

        if (userRepository.existsByEmail(email))
            exist = "Error: Email is already in use!";

        return exist;
    }

    public User createUserAccount(User user, boolean isUser, List<String> roleList) {

        Set<Role> roles = new HashSet<>();

        if (isUser) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);

        } else {
            if (roleList == null) {
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);

            } else {
                roleList.forEach(role -> {
                    switch (role) {
                        case "ROLE_ADMIN":
                            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(adminRole);

                            break;
                        case "ROLE_MANAGER":
                            Role managerRole = roleRepository.findByName(ERole.ROLE_MANAGER)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(managerRole);

                            break;

                        case "ROLE_AUDITOR":
                            Role auditorRole = roleRepository.findByName(ERole.ROLE_AUDITOR)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(auditorRole);

                            break;
                        default:
                            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(userRole);
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

        } else {
            switch (sort) {
                case idAsc:
                    return new Sort.Order(Sort.Direction.ASC, "id");

                case idDesc:
                    return new Sort.Order(Sort.Direction.DESC, "id");

                case nameAsc:
                    return new Sort.Order(Sort.Direction.ASC, "name");

                case nameDesc:
                    return new Sort.Order(Sort.Direction.DESC, "name");

                case lastNameAsc:
                    return new Sort.Order(Sort.Direction.ASC, "lastname");

                case lastNameDesc:
                    return new Sort.Order(Sort.Direction.DESC, "lastname");

                case emailAsc:
                    return new Sort.Order(Sort.Direction.ASC, "email");

                case emailDesc:
                    return new Sort.Order(Sort.Direction.DESC, "email");

                case enabledAsc:
                    return new Sort.Order(Sort.Direction.ASC, "enabled");

                case enabledDesc:
                    return new Sort.Order(Sort.Direction.DESC, "enabled");

                case usernameAsc:
                    return new Sort.Order(Sort.Direction.ASC, "username");

                case usernameDesc:
                    return new Sort.Order(Sort.Direction.DESC, "username");

                case passwordAsc:
                    return new Sort.Order(Sort.Direction.ASC, "password");

                default:
                    return new Sort.Order(Sort.Direction.DESC, "password");
            }
        }
    }
}
