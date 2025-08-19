package com.Alura.Foro_HUB.config;

import com.Alura.Foro_HUB.user.User;
import com.Alura.Foro_HUB.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initAdmin(UserRepository repo, BCryptPasswordEncoder encoder) {
        return args -> {
            if (repo.findByUsername("admin").isEmpty()) {
                repo.save(new User("admin", encoder.encode("admin123"), "ROLE_ADMIN"));
            }
        };
    }
}
