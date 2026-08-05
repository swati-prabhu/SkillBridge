package com.skillbridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "resume_education")
@Getter
@Setter
@NoArgsConstructor
public class ResumeEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(nullable = false, length = 150)
    private String institution;

    @Column(nullable = false, length = 150)
    private String degree;

    @Column(name = "start_year", length = 10)
    private String startYear;

    @Column(name = "end_year", length = 10)
    private String endYear;
}
