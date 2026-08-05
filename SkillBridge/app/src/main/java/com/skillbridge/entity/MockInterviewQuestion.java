package com.skillbridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mock_interview_questions")
@Getter
@Setter
@NoArgsConstructor
public class MockInterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mock_interview_id", nullable = false)
    private MockInterview mockInterview;

    @Column(nullable = false, length = 500)
    private String questionText;

    @Column(length = 500)
    private String idealAnswerHints; // bullet points an admin/AI reviewer checks for - not shown to the student while answering

    @Column(name = "order_index")
    private int orderIndex;
}
