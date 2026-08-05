package com.skillbridge.service;

import com.skillbridge.dto.RegisterRequest;
import com.skillbridge.entity.Company;
import com.skillbridge.entity.Role;
import com.skillbridge.entity.User;
import com.skillbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyService companyService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No account found for " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Role role = Role.valueOf(request.getRole().toUpperCase());
        user.setRole(role);

        if (role == Role.RECRUITER) {
            if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
                throw new IllegalArgumentException("Company name is required to register as a recruiter.");
            }
            Company company = companyService.findOrCreateByName(request.getCompanyName().trim());
            user.setCompany(company);
            // New recruiter accounts start unverified; an admin verifies them before their postings go live-trusted.
            user.setRecruiterVerified(false);
        }

        return userRepository.save(user);
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public List<User> findAllStudents() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.STUDENT)
                .toList();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public List<User> findAllAlumni() {
        return userRepository.findByRoleAndAlumniTrue(Role.STUDENT);
    }

    public long countStudents() {
        return findAllStudents().size();
    }
}
