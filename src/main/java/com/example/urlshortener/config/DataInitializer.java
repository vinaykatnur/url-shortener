package com.example.urlshortener.config;

import com.example.urlshortener.entity.Role;
import com.example.urlshortener.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        ensureRoleExists("ROLE_USER");
        ensureRoleExists("ROLE_ADMIN");
    }

    private void ensureRoleExists(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = new Role();
            role.setName(name);
            roleRepository.save(role);
            log.info("Successfully initialized missing mandatory role: {}", name);
        }
    }
}
