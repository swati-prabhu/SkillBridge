package com.skillbridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mock_interviews")
@Getter
@Setter
@NoArgsConstructor
public class MockInterview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title; // e.g. "Backend Developer Mock Interview"

    @Column(length = 255)
    private String description;

    @Column(length = 80)
    private String track; // matches Roadmap.track for suggestion purposes

    @OneToMany(mappedBy = "mockInterview", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<MockInterviewQuestion> questions = new ArrayList<>();
}
