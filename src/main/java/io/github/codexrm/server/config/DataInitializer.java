package io.github.codexrm.server.config;

import io.github.codexrm.server.enums.ERole;
import io.github.codexrm.server.model.Role;
import io.github.codexrm.server.model.User;
import io.github.codexrm.server.repository.RoleRepository;
import io.github.codexrm.server.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder encoder) {

        return args -> {

            if (userRepository.existsByUsername("admin")) {return;}

            Optional<Role> adminRoleOpt = roleRepository.findByName(ERole.ROLE_ADMIN);

            if (adminRoleOpt.isEmpty()) {
                throw new RuntimeException("Error: ROLE_ADMIN not found in database.");
            }

            Role adminRole = adminRoleOpt.get();

            User admin = new User("admin", "Admin", "System", "admin@codexrm.com", true, encoder.encode("Adm!123"));

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            admin.setRoles(roles);

            userRepository.save(admin);

            System.out.println("✅ Admin user created!");
        };
    }
}