package io.github.codexrm.server.domain.service;

import io.github.codexrm.server.domain.enums.ERole;
import io.github.codexrm.server.infrastructure.exception.ResourceNotFoundException;
import io.github.codexrm.server.domain.model.Role;
import io.github.codexrm.server.infrastructure.persistence.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Set<Role> resolveRoles(List<String> roleNames) {

        Set<Role> roles = new HashSet<>();

        if (roleNames == null || roleNames.isEmpty()) {
            roles.add(getRoleOrThrow(ERole.ROLE_USER));
            return roles;
        }
        
        for (String roleName : roleNames) {
            try {
                ERole eRole = ERole.valueOf(roleName);
                roles.add(getRoleOrThrow(eRole));
            } catch (IllegalArgumentException e) {
                throw new ResourceNotFoundException("Role", "name", roleName);
            }
        }
        return roles;
    }

    private Role getRoleOrThrow(ERole role) {
        return roleRepository.findByName(role)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", role.name()));
    }
}