package com.skillbridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mock_interview_responses")
@Getter
@Setter
@NoArgsConstructor
public class MockInterviewResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "attempt_id", nullable = false)
    private MockInterviewAttempt attempt;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private MockInterviewQuestion question;

    @Column(columnDefinition = "TEXT")
    private String answerText;
}
