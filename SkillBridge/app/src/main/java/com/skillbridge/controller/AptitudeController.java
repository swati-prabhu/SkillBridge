package com.skillbridge.controller;

import com.skillbridge.entity.AptitudeTest;
import com.skillbridge.entity.User;
import com.skillbridge.service.AptitudeService;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/tests")
public class AptitudeController {

    private final AptitudeService aptitudeService;
    private final UserService userService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("tests", aptitudeService.findAllTests());
        return "tests";
    }

    @GetMapping("/{id}/take")
    public String take(@PathVariable Long id, Model model) {
        model.addAttribute("test", aptitudeService.findTestById(id));
        return "take-test";
    }

    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id, @RequestParam Map<String, String> allParams,
                          Authentication auth, Model model) {
        AptitudeTest test = aptitudeService.findTestById(id);
        User student = userService.getByEmail(auth.getName());

        Map<Long, String> answers = new HashMap<>();
        allParams.forEach((key, value) -> {
            if (key.startsWith("q_")) {
                Long questionId = Long.valueOf(key.substring(2));
                answers.put(questionId, value);
            }
        });

        var result = aptitudeService.submit(student, test, answers);
        model.addAttribute("result", result);
        return "test-result";
    }

    @GetMapping("/results")
    public String myResults(Authentication auth, Model model) {
        User student = userService.getByEmail(auth.getName());
        model.addAttribute("results", aptitudeService.findResultsByStudent(student));
        return "test-results";
    }
}
