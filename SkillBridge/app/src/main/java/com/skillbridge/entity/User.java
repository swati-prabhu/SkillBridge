package com.skillbridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "resume_path")
    private String resumePath;

    @Column(length = 255)
    private String skills; // comma-separated, used for naive matching/recommendations

    // ── Student-specific ──
    @Column(length = 60)
    private String department;

    @Column(name = "graduation_year")
    private Integer graduationYear;

    @Column(name = "is_alumni")
    private boolean alumni = false;

    // ── Recruiter-specific ──
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(length = 100)
    private String designation; // e.g. "Talent Acquisition Lead"

    @Column(name = "recruiter_verified")
    private boolean recruiterVerified = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
