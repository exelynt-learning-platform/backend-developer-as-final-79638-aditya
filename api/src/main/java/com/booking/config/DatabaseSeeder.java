package com.booking.config;

import com.booking.model.Role;
import com.booking.model.User;
import com.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Check if users already exist so we don't duplicate them on restart
        if (userRepository.count() == 0) {

            User admin = User.builder()
                    .email("admin@system.local")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();

            User user = User.builder()
                    .email("user@system.local")
                    .password(passwordEncoder.encode("user123"))
                    .role(Role.USER)
                    .build();

            userRepository.save(admin);
            userRepository.save(user);

            System.out.println("Congratulations Database seeded with default ADMIN and USER accounts.");
        }
    }
}