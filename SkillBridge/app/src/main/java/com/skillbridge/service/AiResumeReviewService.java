package com.skillbridge.service;

import com.skillbridge.entity.Resume;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * "AI Resume Review" - despite the label recruiters/students expect, this is
 * a deterministic rule-based reviewer, not a call to an LLM (no API key is
 * configured for this project). It reuses the same signals as
 * ResumeScoreService but produces human-readable, actionable feedback lines
 * instead of just a number. Swapping this for a real LLM call later is a
 * drop-in change: replace the body of review() with a prompt built from the
 * same Resume fields and parse the model's response into the same
 * ReviewResult shape.
 */
@Service
public class AiResumeReviewService {

    public static class ReviewResult {
        public int atsScore;
        public List<String> strengths = new ArrayList<>();
        public List<String> issues = new ArrayList<>();
        public List<String> suggestions = new ArrayList<>();
    }

    public ReviewResult review(Resume resume, String skillsCsv, int resumeScore) {
        ReviewResult result = new ReviewResult();
        result.atsScore = resumeScore;

        int skillCount = skillsCsv == null || skillsCsv.isBlank() ? 0
                : (int) java.util.Arrays.stream(skillsCsv.split(",")).filter(s -> !s.isBlank()).count();

        // Strengths
        if (resume.getProjects().size() >= 2) result.strengths.add("Good project count (" + resume.getProjects().size() + ") - shows initiative beyond coursework.");
        if (resume.getExperience().size() >= 1) result.strengths.add("Has real work/internship experience listed, which recruiters weight heavily.");
        if (skillCount >= 5) result.strengths.add("Skills section is well populated (" + skillCount + " skills).");
        if (resume.getSummary() != null && resume.getSummary().length() > 50) result.strengths.add("Has a clear summary stating career direction.");
        boolean hasQuantifiedExperience = resume.getExperience().stream()
                .anyMatch(e -> e.getDescription() != null && e.getDescription().matches(".*\\d.*"));
        if (hasQuantifiedExperience) result.strengths.add("At least one experience bullet includes a number/metric - stronger than generic descriptions.");

        // Issues + suggestions (the "missing projects/skills, actionable" part of the ask)
        if (resume.getProjects().isEmpty()) {
            result.issues.add("No projects listed.");
            result.suggestions.add("Add 1-2 projects, even class projects - include the tech stack and what YOU specifically built.");
        }
        if (resume.getExperience().isEmpty()) {
            result.issues.add("No work/internship experience listed.");
            result.suggestions.add("If you lack formal experience, list significant projects under an 'Experience' framing with impact-oriented bullets instead.");
        }
        if (skillCount < 5) {
            result.issues.add("Skills section is thin (" + skillCount + " skills listed).");
            result.suggestions.add("List specific technologies (e.g. 'Spring Boot' not just 'backend'), aiming for 6-10 relevant skills.");
        }
        if (resume.getSummary() == null || resume.getSummary().isBlank()) {
            result.issues.add("No summary/headline.");
            result.suggestions.add("Add a 2-3 sentence summary at the top: who you are, your focus area, and one standout achievement.");
        }
        if (!hasQuantifiedExperience && !resume.getExperience().isEmpty()) {
            result.issues.add("Experience descriptions don't include measurable impact.");
            result.suggestions.add("Rewrite experience bullets to include a number where possible (e.g. 'reduced load time by 30%', 'handled 500+ requests/day').");
        }
        boolean hasAnyLink = notBlank(resume.getGithub()) || notBlank(resume.getLinkedin()) || notBlank(resume.getPortfolio());
        if (!hasAnyLink) {
            result.issues.add("No GitHub, LinkedIn, or portfolio link.");
            result.suggestions.add("Add at least a GitHub link so recruiters can verify your project claims.");
        }

        if (result.issues.isEmpty()) {
            result.suggestions.add("Resume covers the fundamentals well - focus next on tailoring the summary and skills order per job application.");
        }

        return result;
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
