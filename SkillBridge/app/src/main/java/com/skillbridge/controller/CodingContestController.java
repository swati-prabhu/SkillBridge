package com.skillbridge.controller;

import com.skillbridge.entity.AptitudeTest;
import com.skillbridge.entity.TestResult;
import com.skillbridge.entity.User;
import com.skillbridge.repository.AptitudeTestRepository;
import com.skillbridge.repository.TestResultRepository;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.List;

/**
 * A "coding contest" is a time-boxed AptitudeTest (contest=true) with
 * optional external coding-problem links, plus a leaderboard ranked by
 * score desc, then submission time asc (faster correct submission wins ties)
 * - reuses all the existing test-taking infrastructure rather than
 * duplicating it.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/contests")
public class CodingContestController {

    private final AptitudeTestRepository aptitudeTestRepository;
    private final TestResultRepository testResultRepository;
    private final UserService userService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("contests", aptitudeTestRepository.findByContestTrue());
        return "contests";
    }

    @GetMapping("/{id}/leaderboard")
    public String leaderboard(@PathVariable Long id, Authentication auth, Model model) {
        AptitudeTest contest = aptitudeTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contest not found: " + id));

        List<TestResult> results = testResultRepository.findAll().stream()
                .filter(r -> r.getTest().getId().equals(id))
                .sorted(Comparator.<TestResult>comparingInt(r -> -r.getScore()).thenComparing(TestResult::getTakenAt))
                .toList();

        User currentUser = userService.getByEmail(auth.getName());

        model.addAttribute("contest", contest);
        model.addAttribute("results", results);
        model.addAttribute("currentUserId", currentUser.getId());
        return "contest-leaderboard";
    }
}
