package com.skillbridge.controller;

import com.skillbridge.entity.User;
import com.skillbridge.service.InternshipService;
import com.skillbridge.service.SkillGapService;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/skill-gap")
public class SkillGapController {

    private final InternshipService internshipService;
    private final SkillGapService skillGapService;
    private final UserService userService;

    @GetMapping
    public String analyze(@RequestParam(required = false) Long internshipId, Authentication auth, Model model) {
        User student = userService.getByEmail(auth.getName());
        model.addAttribute("internships", internshipService.findAll());

        if (internshipId != null) {
            var target = internshipService.findById(internshipId);
            var result = skillGapService.analyze(student.getSkills(), target);
            model.addAttribute("target", target);
            model.addAttribute("result", result);
        }
        return "skill-gap";
    }
}
