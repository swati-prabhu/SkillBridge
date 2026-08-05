package com.skillbridge.service;

import com.skillbridge.entity.*;
import com.skillbridge.repository.AptitudeTestRepository;
import com.skillbridge.repository.TestResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AptitudeService {

    private final AptitudeTestRepository testRepository;
    private final TestResultRepository resultRepository;
    private final NotificationService notificationService;

    public List<AptitudeTest> findAllTests() {
        return testRepository.findAll();
    }

    public AptitudeTest findTestById(Long id) {
        return testRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test not found: " + id));
    }

    public AptitudeTest saveTest(AptitudeTest test) {
        return testRepository.save(test);
    }

    /**
     * submittedAnswers: map of questionId -> selected option ("A"/"B"/"C"/"D")
     */
    public TestResult submit(User student, AptitudeTest test, Map<Long, String> submittedAnswers) {
        int score = 0;
        for (Question q : test.getQuestions()) {
            String submitted = submittedAnswers.get(q.getId());
            if (submitted != null && submitted.equalsIgnoreCase(q.getCorrectOption())) {
                score++;
            }
        }
        TestResult result = new TestResult();
        result.setStudent(student);
        result.setTest(test);
        result.setScore(score);
        result.setTotalQuestions(test.getQuestions().size());
        TestResult saved = resultRepository.save(result);

        int percent = test.getQuestions().isEmpty() ? 0 : (int) Math.round((100.0 * score) / test.getQuestions().size());
        notificationService.notify(student,
                "You scored " + score + "/" + test.getQuestions().size() + " (" + percent + "%) on \"" + test.getTitle() + "\".",
                "/tests/results");

        return saved;
    }

    public List<TestResult> findResultsByStudent(User student) {
        return resultRepository.findByStudent(student);
    }

    public TestResult findResultById(Long id) {
        return resultRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test result not found: " + id));
    }

    public double averageScorePercent() {
        List<TestResult> all = resultRepository.findAll();
        if (all.isEmpty()) return 0.0;
        double totalPercent = all.stream()
                .mapToDouble(r -> r.getTotalQuestions() == 0 ? 0 : (100.0 * r.getScore() / r.getTotalQuestions()))
                .sum();
        return Math.round((totalPercent / all.size()) * 100.0) / 100.0;
    }

    /** Average score percent for one specific student - used on their personal progress page. */
    public double averageScorePercent(User student) {
        List<TestResult> results = resultRepository.findByStudent(student);
        if (results.isEmpty()) return 0.0;
        double totalPercent = results.stream()
                .mapToDouble(r -> r.getTotalQuestions() == 0 ? 0 : (100.0 * r.getScore() / r.getTotalQuestions()))
                .sum();
        return Math.round((totalPercent / results.size()) * 100.0) / 100.0;
    }
}
