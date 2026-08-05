package com.skillbridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "internships")
@Getter
@Setter
@NoArgsConstructor
public class Internship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 150)
    private String company;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 120)
    private String location;

    @Column(length = 50)
    private String stipend;

    @Column(length = 50)
    private String duration;

    @Column(name = "required_skills", length = 255)
    private String requiredSkills; // comma-separated

    @Column(length = 60)
    private String category = "General";

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company companyRef; // set when posted by a verified recruiter; null for legacy/admin-posted internships

    @ManyToOne
    @JoinColumn(name = "posted_by")
    private User postedBy;

    @ManyToOne
    @JoinColumn(name = "drive_id")
    private PlacementDrive drive;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
