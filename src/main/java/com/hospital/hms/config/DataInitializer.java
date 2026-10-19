package com.hospital.hms.config;

import com.hospital.hms.entity.Role;
import com.hospital.hms.entity.User;
import com.hospital.hms.repository.RoleRepository;
import com.hospital.hms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedRoles();
        seedAdminUser();
        seedDoctorUser();
        seedPatientUser();
        seedReceptionistUser();
    }

    private void seedRoles() {
        String[] roleNames = { "ROLE_ADMIN", "ROLE_DOCTOR", "ROLE_PATIENT", "ROLE_RECEPTIONIST" };

        for (String name : roleNames) {
            if (roleRepository.findByName(name).isEmpty()) {
                Role role = Role.builder().name(name).build();
                roleRepository.save(role);
                System.out.println("✅ Seeded role: " + name);
            }
        }
    }

    private void seedAdminUser() {
        seedUser("admin", "admin123", "admin@hms.com", "ROLE_ADMIN");
    }

    private void seedDoctorUser() {
        seedUser("doctor1", "doctor123", "doctor1@hms.com", "ROLE_DOCTOR");
    }

    private void seedPatientUser() {
        seedUser("patient1", "patient123", "patient1@hms.com", "ROLE_PATIENT");
    }

    private void seedReceptionistUser() {
        seedUser("reception1", "reception123", "reception1@hms.com", "ROLE_RECEPTIONIST");
    }

    private void seedUser(String username, String rawPassword, String email, String roleName) {
        if (userRepository.existsByUsername(username)) {
            System.out.println("ℹ️ User already exists — skipping: " + username);
            return;
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException(roleName + " not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .email(email)
                .active(true)
                .roles(roles)
                .build();

        userRepository.save(user);
        System.out.println("✅ Seeded user: " + username + " / " + rawPassword + " (" + roleName + ")");
    }
}