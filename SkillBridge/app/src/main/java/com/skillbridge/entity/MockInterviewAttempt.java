package com.skillbridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mock_interview_attempts")
@Getter
@Setter
@NoArgsConstructor
public class MockInterviewAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "mock_interview_id", nullable = false)
    private MockInterview mockInterview;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "reviewed")
    private boolean reviewed = false;

    @Column(name = "overall_feedback", length = 1000)
    private String overallFeedback; // filled in by admin, or auto-generated heuristic feedback

    @Column(name = "overall_score")
    private Integer overallScore; // 0-100, set on review

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockInterviewResponse> responses = new ArrayList<>();
}
