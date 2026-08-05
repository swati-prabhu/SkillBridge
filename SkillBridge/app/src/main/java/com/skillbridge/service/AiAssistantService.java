package com.skillbridge.service;

import com.skillbridge.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * "AI Placement Assistant" - a rule-based intent router over the app's
 * existing services (recommendations, resume, applications, roadmaps,
 * interviews), NOT a call to a real language model - no LLM API key is
 * configured for this project. It matches keywords in the student's message
 * to an intent and calls the same service methods the MCP server exposes to
 * external AI agents, so the logic is defined exactly once either way.
 * Swapping in a real LLM later means replacing route() with a call to
 * an actual model, optionally *keeping* these same service calls as tools
 * the model can invoke (which is exactly the MCP pattern already used
 * elsewhere in this project).
 */
@Service
@RequiredArgsConstructor
public class AiAssistantService {

    private final InternshipService internshipService;
    private final ApplicationService applicationService;
    private final ResumeService resumeService;
    private final RoadmapService roadmapService;
    private final InterviewService interviewService;
    private final AptitudeService aptitudeService;

    public String respond(User student, String message) {
        String text = message == null ? "" : message.toLowerCase();

        if (containsAny(text, "recommend", "match", "suggest internship", "internships for me")) {
            return handleRecommendations(student);
        }
        if (containsAny(text, "resume", "cv")) {
            return handleResume(student);
        }
        if (containsAny(text, "pending", "my application", "applications", "status of my")) {
            return handleApplications(student);
        }
        if (containsAny(text, "roadmap", "learning path", "what should i learn")) {
            return handleRoadmap(student);
        }
        if (containsAny(text, "interview", "schedule")) {
            return handleInterviews(student);
        }
        if (containsAny(text, "aptitude", "test score", "my score")) {
            return handleAptitude(student);
        }

        return "I can help with: internship recommendations, resume feedback, your application status, "
                + "roadmap suggestions, upcoming interviews, or your aptitude test scores. "
                + "Try asking something like \"What internships match my skills?\" or \"How's my resume looking?\"";
    }

    private String handleRecommendations(User student) {
        var recs = internshipService.recommendWeighted(student.getSkills(), 3);
        if (recs.isEmpty()) {
            return "I don't have enough skills on your profile to recommend anything yet. "
                    + "Add some skills on your Profile or Resume Builder page and ask me again.";
        }
        String list = recs.stream()
                .map(r -> "• " + r.getInternship().getTitle() + " at " + r.getInternship().getCompany()
                        + " (" + r.getMatchPercent() + "% match) — " + r.getExplanation())
                .collect(Collectors.joining("\n"));
        return "Here are your top matches:\n" + list;
    }

    private String handleResume(User student) {
        Resume resume = resumeService.findOrCreate(student);
        if (resume.getId() == null) {
            return "You haven't built a resume yet. Head to the Resume Builder to get started — I can give you feedback once you have.";
        }
        return "Your resume score is " + resume.getScore() + "/100. "
                + "Check the Resume Builder page for a full AI Resume Review with specific suggestions.";
    }

    private String handleApplications(User student) {
        List<Application> applications = applicationService.findByStudent(student);
        if (applications.isEmpty()) {
            return "You haven't applied to any internships yet. Check the Internships page to get started.";
        }
        String pending = applications.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.APPLIED || a.getStatus() == ApplicationStatus.SHORTLISTED)
                .map(a -> "• " + a.getInternship().getTitle() + " at " + a.getInternship().getCompany() + " — " + a.getStatus())
                .collect(Collectors.joining("\n"));
        return applications.size() + " total application(s). "
                + (pending.isBlank() ? "None currently pending a decision." : "Pending:\n" + pending);
    }

    private String handleRoadmap(User student) {
        var recommended = roadmapService.recommendForSkills(student.getSkills());
        if (recommended.isPresent()) {
            Roadmap r = recommended.get();
            int percent = roadmapService.percentComplete(student, r);
            return "Based on your skills, \"" + r.getTitle() + "\" is your best-fit roadmap — you're " + percent + "% through it.";
        }
        int overall = roadmapService.overallPercentComplete(student);
        return "I don't have a specific roadmap recommendation yet - add some skills to your profile. "
                + "Your overall roadmap completion across everything available is " + overall + "%.";
    }

    private String handleInterviews(User student) {
        var interviews = interviewService.findByStudent(student);
        if (interviews.isEmpty()) {
            return "You don't have any interviews scheduled right now.";
        }
        String list = interviews.stream()
                .limit(3)
                .map(i -> "• " + i.getApplication().getInternship().getTitle() + " — "
                        + i.getScheduledAt().toLocalDate() + " at " + i.getScheduledAt().toLocalTime() + " (" + i.getMode() + ")")
                .collect(Collectors.joining("\n"));
        return "Your upcoming interviews:\n" + list;
    }

    private String handleAptitude(User student) {
        double avg = aptitudeService.averageScorePercent(student);
        var results = aptitudeService.findResultsByStudent(student);
        if (results.isEmpty()) {
            return "You haven't taken any aptitude tests yet - try one from the Aptitude Tests page.";
        }
        return "You've taken " + results.size() + " test(s), averaging " + avg + "%.";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }
}
