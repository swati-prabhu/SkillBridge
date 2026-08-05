package com.skillbridge.config;

import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/register", "/login", "/css/**", "/js/**", "/images/**", "/ws/**").permitAll()
                .requestMatchers("/internships/new", "/internships/*/edit", "/internships/*/delete").hasAnyAuthority("ROLE_ADMIN", "ROLE_RECRUITER")
                .requestMatchers("/recruiter/**").hasAuthority("ROLE_RECRUITER")
                .requestMatchers("/applications/manage", "/applications/*/schedule-interview", "/applications/*/status",
                                  "/analytics", "/placement-statistics", "/tests/manage/**", "/admin/**", "/mock-interviews/manage/**", "/mock-interviews/attempts/*/review",
                                  "/forum/moderate", "/forum/*/moderate", "/reports/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .csrf(AbstractHttpConfigurer::disable); // simplified for demo; enable + wire CSRF tokens in templates for production

        return http.build();
    }
}
