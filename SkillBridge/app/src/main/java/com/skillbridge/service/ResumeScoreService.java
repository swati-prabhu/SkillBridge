package com.skillbridge.service;

import com.skillbridge.entity.Resume;
import org.springframework.stereotype.Service;

/**
 * A transparent, heuristic resume completeness score (0-100).
 * Deliberately simple and explainable rather than ML-based - easier
 * to defend in an interview than a black-box score.
 */
@Service
public class ResumeScoreService {

    public int calculate(Resume resume, String skillsCsv) {
        int score = 0;

        if (resume.getHeadline() != null && resume.getHeadline().length() > 10) score += 10;
        if (resume.getSummary() != null && resume.getSummary().length() > 50) score += 15;

        if (!resume.getEducation().isEmpty()) score += 15;

        if (!resume.getExperience().isEmpty()) score += 15;
        if (resume.getExperience().size() > 1) score += 5;
        boolean hasDescriptiveExperience = resume.getExperience().stream()
                .anyMatch(e -> e.getDescription() != null && e.getDescription().length() > 40);
        if (hasDescriptiveExperience) score += 10;

        if (!resume.getProjects().isEmpty()) score += 10;
        if (resume.getProjects().size() > 1) score += 5;

        int skillCount = skillsCsv == null || skillsCsv.isBlank()
                ? 0
                : (int) java.util.Arrays.stream(skillsCsv.split(",")).filter(s -> !s.isBlank()).count();
        if (skillCount >= 5) score += 10;
        else if (skillCount > 0) score += 5;

        boolean hasLink = notBlank(resume.getGithub()) || notBlank(resume.getLinkedin()) || notBlank(resume.getPortfolio());
        if (hasLink) score += 5;

        return Math.min(100, score);
    }

    public String label(int score) {
        if (score >= 80) return "Excellent";
        if (score >= 55) return "Good";
        return "Needs work";
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
