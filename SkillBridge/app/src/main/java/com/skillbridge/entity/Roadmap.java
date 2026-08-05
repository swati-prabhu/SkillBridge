package com.skillbridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roadmaps")
@Getter
@Setter
@NoArgsConstructor
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 255)
    private String description;

    @Column(length = 80)
    private String track; // e.g. "Backend Developer", "Frontend Developer", "Data Analyst"

    @Column(length = 40)
    private String icon = "bi-map"; // bootstrap icon class

    @Column(name = "related_skills", length = 255)
    private String relatedSkills; // comma-separated; used to personalize/recommend this roadmap to a student

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<RoadmapStep> steps = new ArrayList<>();
}
