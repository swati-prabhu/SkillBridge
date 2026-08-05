package com.skillbridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Anonymous by design: the student who submitted it is stored (to enforce
 * one rating per student per company) but is never exposed in any view -
 * only aggregated averages and free-text comments are shown.
 */
@Entity
@Table(name = "recruiter_ratings", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "company_id"}))
@Getter
@Setter
@NoArgsConstructor
public class RecruiterRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private int difficulty; // 1-5

    @Column(nullable = false)
    private int communication; // 1-5

    @Column(nullable = false)
    private int process; // 1-5

    @Column(length = 500)
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
