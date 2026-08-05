package com.skillbridge.controller;

import com.skillbridge.entity.*;
import com.skillbridge.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ProgressController {

    private final UserService userService;
    private final ResumeService resumeService;
    private final AptitudeService aptitudeService;
    private final RoadmapService roadmapService;
    private final ApplicationService applicationService;
    private final BookmarkService bookmarkService;

    @GetMapping("/progress")
    public String myProgress(Authentication auth, Model model) {
        User student = userService.getByEmail(auth.getName());

        int resumeScore = resumeService.findOrCreate(student).getScore();
        double aptitudeAvg = aptitudeService.averageScorePercent(student);
        int roadmapOverall = roadmapService.overallPercentComplete(student);

        // A single composite number, weighted toward the things a recruiter would actually check first.
        int readinessScore = (int) Math.round(resumeScore * 0.4 + aptitudeAvg * 0.3 + roadmapOverall * 0.3);

        List<Application> applications = applicationService.findByStudent(student);
        Map<String, Long> statusCounts = applications.stream()
                .collect(java.util.stream.Collectors.groupingBy(a -> a.getStatus().name(), java.util.stream.Collectors.counting()));

        List<TestResult> testHistory = aptitudeService.findResultsByStudent(student);
        List<Map<String, Object>> testHistoryForChart = testHistory.stream()
                .sorted(java.util.Comparator.comparing(TestResult::getTakenAt))
                .map(r -> Map.<String, Object>of("score", r.getScore(), "totalQuestions", r.getTotalQuestions()))
                .toList();

        List<Roadmap> roadmaps = roadmapService.findAll();
        List<Map<String, Object>> roadmapProgress = roadmaps.stream().map(r -> Map.<String, Object>of(
                "id", r.getId(),
                "title", r.getTitle(),
                "icon", r.getIcon(),
                "percent", roadmapService.percentComplete(student, r)
        )).toList();

        Optional<Roadmap> recommendedRoadmap = roadmapService.recommendForSkills(student.getSkills());

        model.addAttribute("user", student);
        model.addAttribute("resumeScore", resumeScore);
        model.addAttribute("aptitudeAvg", aptitudeAvg);
        model.addAttribute("roadmapOverall", roadmapOverall);
        model.addAttribute("readinessScore", readinessScore);
        model.addAttribute("applicationsCount", applications.size());
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("testHistory", testHistoryForChart);
        model.addAttribute("roadmapProgress", roadmapProgress);
        model.addAttribute("recommendedRoadmap", recommendedRoadmap.orElse(null));
        model.addAttribute("bookmarksCount", bookmarkService.findByStudent(student).size());

        return "progress";
    }
}
