package io.github.codexrm.server.infrastructure.config;

import io.github.codexrm.server.domain.enums.ERole;
import io.github.codexrm.server.domain.model.Role;
import io.github.codexrm.server.domain.model.User;
import io.github.codexrm.server.infrastructure.persistence.repository.RoleRepository;
import io.github.codexrm.server.infrastructure.persistence.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Configuration
@Profile("!test")
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder encoder) {

        return args -> {

            if (userRepository.existsByUsername("admin")) {return;}

            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: ROLE_ADMIN not found in database."));

            String initialPassword = System.getenv("ADMIN_INITIAL_PASSWORD");
            if (initialPassword == null || initialPassword.isBlank()) {
                throw new IllegalStateException(
                        "ADMIN_INITIAL_PASSWORD environment variable must be set to create the initial admin user.");
            }

            User admin = new User("admin", "Admin", "System", "admin@codexrm.com", true, encoder.encode(initialPassword));
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            admin.setRoles(roles);

            userRepository.save(admin);

            System.out.println("✅ Admin user created!");
        };
    }
}