package com.skillbridge.controller;

import com.skillbridge.entity.*;
import com.skillbridge.service.MockInterviewService;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mock-interviews")
public class MockInterviewController {

    private final MockInterviewService mockInterviewService;
    private final UserService userService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("mockInterviews", mockInterviewService.findAll());
        return "mock-interviews";
    }

    @GetMapping("/{id}/take")
    public String take(@PathVariable Long id, Model model) {
        model.addAttribute("mockInterview", mockInterviewService.findById(id));
        return "mock-interview-take";
    }

    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id, @RequestParam Map<String, String> allParams, Authentication auth, Model model) {
        MockInterview mockInterview = mockInterviewService.findById(id);
        User student = userService.getByEmail(auth.getName());

        Map<Long, String> answers = new HashMap<>();
        allParams.forEach((key, value) -> {
            if (key.startsWith("q_")) {
                answers.put(Long.valueOf(key.substring(2)), value);
            }
        });

        MockInterviewAttempt attempt = mockInterviewService.submit(student, mockInterview, answers);
        return "redirect:/mock-interviews/attempts/" + attempt.getId();
    }

    @GetMapping("/attempts")
    public String myAttempts(Authentication auth, Model model) {
        User student = userService.getByEmail(auth.getName());
        model.addAttribute("attempts", mockInterviewService.findByStudent(student));
        return "mock-interview-attempts";
    }

    @GetMapping("/attempts/{id}")
    public String viewAttempt(@PathVariable Long id, Authentication auth, Model model) {
        User user = userService.getByEmail(auth.getName());
        MockInterviewAttempt attempt = mockInterviewService.findAttemptById(id);

        boolean isOwner = attempt.getStudent().getId().equals(user.getId());
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        model.addAttribute("attempt", attempt);
        return "mock-interview-attempt-detail";
    }

    // ── Admin: manage question banks + review pending attempts ──

    @GetMapping("/manage")
    public String manage(Model model) {
        model.addAttribute("mockInterviews", mockInterviewService.findAll());
        model.addAttribute("pendingReview", mockInterviewService.findPendingReview());
        return "admin-mock-interviews";
    }

    @PostMapping("/attempts/{id}/review")
    public String review(@PathVariable Long id, @RequestParam int score, @RequestParam String feedback) {
        mockInterviewService.reviewAttempt(id, score, feedback);
        return "redirect:/mock-interviews/manage";
    }
}
