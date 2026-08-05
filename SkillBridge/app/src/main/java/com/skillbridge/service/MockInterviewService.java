package com.skillbridge.service;

import com.skillbridge.entity.*;
import com.skillbridge.repository.MockInterviewAttemptRepository;
import com.skillbridge.repository.MockInterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MockInterviewService {

    private final MockInterviewRepository mockInterviewRepository;
    private final MockInterviewAttemptRepository attemptRepository;
    private final NotificationService notificationService;

    public List<MockInterview> findAll() {
        return mockInterviewRepository.findAll();
    }

    public MockInterview findById(Long id) {
        return mockInterviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mock interview not found: " + id));
    }

    public MockInterview save(MockInterview mockInterview) {
        return mockInterviewRepository.save(mockInterview);
    }

    public List<MockInterviewAttempt> findByStudent(User student) {
        return attemptRepository.findByStudentOrderBySubmittedAtDesc(student);
    }

    public List<MockInterviewAttempt> findPendingReview() {
        return attemptRepository.findByReviewedFalseOrderBySubmittedAtAsc();
    }

    public MockInterviewAttempt findAttemptById(Long id) {
        return attemptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + id));
    }

    /**
     * Submits answers and immediately runs a heuristic auto-review (keyword
     * overlap between the student's answer and the question's ideal-answer
     * hints) so the student gets *some* feedback right away, ahead of an
     * admin doing a real review. Not an LLM - a transparent keyword check.
     */
    public MockInterviewAttempt submit(User student, MockInterview mockInterview, Map<Long, String> answers) {
        MockInterviewAttempt attempt = new MockInterviewAttempt();
        attempt.setStudent(student);
        attempt.setMockInterview(mockInterview);

        int totalHeuristicScore = 0;
        int scoredQuestions = 0;

        for (MockInterviewQuestion q : mockInterview.getQuestions()) {
            MockInterviewResponse response = new MockInterviewResponse();
            response.setAttempt(attempt);
            response.setQuestion(q);
            response.setAnswerText(answers.getOrDefault(q.getId(), ""));
            attempt.getResponses().add(response);

            if (q.getIdealAnswerHints() != null && !q.getIdealAnswerHints().isBlank()) {
                totalHeuristicScore += heuristicScore(response.getAnswerText(), q.getIdealAnswerHints());
                scoredQuestions++;
            }
        }

        if (scoredQuestions > 0) {
            attempt.setOverallScore(totalHeuristicScore / scoredQuestions);
            attempt.setOverallFeedback("Auto-generated preliminary feedback based on keyword coverage of expected talking points. "
                    + "An admin review will replace this with more detailed, human feedback.");
        }

        MockInterviewAttempt saved = attemptRepository.save(attempt);

        notificationService.notify(student,
                "Your mock interview \"" + mockInterview.getTitle() + "\" was submitted"
                        + (saved.getOverallScore() != null ? " - preliminary score: " + saved.getOverallScore() + "%." : "."),
                "/mock-interviews/attempts/" + saved.getId());

        return saved;
    }

    /** % of hint keywords that appear (case-insensitive substring) somewhere in the student's answer. */
    private int heuristicScore(String answer, String hints) {
        if (answer == null || answer.isBlank()) return 0;
        String lowerAnswer = answer.toLowerCase();
        String[] keywords = hints.toLowerCase().split(",");
        long hitCount = Arrays.stream(keywords)
                .map(String::trim)
                .filter(k -> !k.isEmpty())
                .filter(lowerAnswer::contains)
                .count();
        long totalKeywords = Arrays.stream(keywords).map(String::trim).filter(k -> !k.isEmpty()).count();
        return totalKeywords == 0 ? 0 : (int) Math.round((hitCount * 100.0) / totalKeywords);
    }

    public MockInterviewAttempt reviewAttempt(Long attemptId, int score, String feedback) {
        MockInterviewAttempt attempt = findAttemptById(attemptId);
        attempt.setOverallScore(score);
        attempt.setOverallFeedback(feedback);
        attempt.setReviewed(true);
        MockInterviewAttempt saved = attemptRepository.save(attempt);

        notificationService.notify(attempt.getStudent(),
                "Your mock interview \"" + attempt.getMockInterview().getTitle() + "\" has been reviewed - score: " + score + "%.",
                "/mock-interviews/attempts/" + attempt.getId());

        return saved;
    }
}
