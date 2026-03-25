package io.github.codexrm.server.config;

import io.github.codexrm.server.model.Role;
import io.github.codexrm.server.enums.ERole;
import io.github.codexrm.server.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoleInitializer {

    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {

            for (ERole roleEnum : ERole.values()) {

                roleRepository.findByName(roleEnum)
                        .orElseGet(() -> {
                            Role role = new Role();
                            role.setName(roleEnum);
                            return roleRepository.save(role);
                        });
            }
        };
    }
}