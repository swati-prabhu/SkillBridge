package com.skillbridge.controller;

import com.skillbridge.dto.AnalyticsSummary;
import com.skillbridge.entity.Internship;
import com.skillbridge.entity.User;
import com.skillbridge.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final InternshipService internshipService;
    private final ApplicationService applicationService;
    private final AnalyticsService analyticsService;
    private final ResumeService resumeService;
    private final InterviewService interviewService;
    private final PlacementDriveService placementDriveService;
    private final RoadmapService roadmapService;
    private final RecruiterRatingService recruiterRatingService;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User user = userService.getByEmail(auth.getName());
        model.addAttribute("user", user);

        switch (user.getRole()) {
            case ADMIN -> {
                model.addAttribute("totalInternships", internshipService.count());
                model.addAttribute("totalApplications", applicationService.count());
                model.addAttribute("totalDrives", placementDriveService.findAll().size());
                return "dashboard-admin";
            }
            case RECRUITER -> {
                List<Internship> myInternships = internshipService.findByPostedBy(user.getId());
                List<Long> myInternshipIds = myInternships.stream().map(Internship::getId).toList();
                var myApplications = applicationService.findByInternshipIds(myInternshipIds);

                model.addAttribute("myInternships", myInternships);
                model.addAttribute("totalApplicants", myApplications.size());
                model.addAttribute("pendingReview", myApplications.stream()
                        .filter(a -> a.getStatus() == com.skillbridge.entity.ApplicationStatus.APPLIED).count());
                model.addAttribute("interviewsScheduled", myApplications.stream()
                        .filter(a -> a.getStatus() == com.skillbridge.entity.ApplicationStatus.INTERVIEW_SCHEDULED).count());
                if (user.getCompany() != null) {
                    model.addAttribute("ratingSummary", recruiterRatingService.summarize(user.getCompany()));
                }
                return "dashboard-recruiter";
            }
            default -> {
                model.addAttribute("myApplications", applicationService.findByStudent(user));
                model.addAttribute("internships", internshipService.findAll());
                model.addAttribute("resumeScore", resumeService.findOrCreate(user).getScore());
                model.addAttribute("interviewCount", interviewService.findByStudent(user).size());
                model.addAttribute("recommendedRoadmap", roadmapService.recommendForSkills(user.getSkills()).orElse(null));

                List<Map.Entry<Internship, Integer>> recommended = internshipService.recommendForSkills(user.getSkills(), 3);
                model.addAttribute("recommended", recommended);
                return "dashboard-student";
            }
        }
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        model.addAttribute("summary", analyticsService.buildSummary());
        return "analytics";
    }

    /** JSON endpoint consumed by Chart.js on the analytics page (same data the MCP server exposes). */
    @RestController
    @RequiredArgsConstructor
    public static class AnalyticsApi {
        private final AnalyticsService analyticsService;

        @GetMapping("/api/analytics/summary")
        public AnalyticsSummary summary() {
            return analyticsService.buildSummary();
        }
    }
}
