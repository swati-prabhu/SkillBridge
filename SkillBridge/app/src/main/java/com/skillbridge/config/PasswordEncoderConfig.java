package com.skillbridge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Kept separate from SecurityConfig on purpose: SecurityConfig depends on UserService,
 * and UserService depends on PasswordEncoder. If PasswordEncoder were defined as a @Bean
 * method inside SecurityConfig, Spring would need to fully construct SecurityConfig
 * (which needs UserService) just to produce the PasswordEncoder that UserService needs —
 * a circular dependency. Defining it here breaks that cycle.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
