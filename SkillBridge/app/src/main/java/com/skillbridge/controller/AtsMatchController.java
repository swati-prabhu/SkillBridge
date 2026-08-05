package com.skillbridge.controller;

import com.skillbridge.entity.Internship;
import com.skillbridge.entity.Resume;
import com.skillbridge.entity.User;
import com.skillbridge.service.AtsMatchService;
import com.skillbridge.service.InternshipService;
import com.skillbridge.service.ResumeService;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AtsMatchController {

    private final InternshipService internshipService;
    private final ResumeService resumeService;
    private final UserService userService;
    private final AtsMatchService atsMatchService;

    @GetMapping("/internships/{id}/ats-match")
    public String matchPage(@PathVariable Long id, Authentication auth, Model model) {
        User student = userService.getByEmail(auth.getName());
        Internship internship = internshipService.findById(id);
        Resume resume = resumeService.findOrCreate(student);

        var result = atsMatchService.match(resume, student.getSkills(), internship.getRequiredSkills(), internship.getDescription());

        model.addAttribute("internship", internship);
        model.addAttribute("result", result);
        return "ats-match";
    }

    @PostMapping("/internships/{id}/ats-match/custom")
    public String matchCustomJd(@PathVariable Long id, @RequestParam String jobDescriptionText, Authentication auth, Model model) {
        User student = userService.getByEmail(auth.getName());
        Internship internship = internshipService.findById(id);
        Resume resume = resumeService.findOrCreate(student);

        var result = atsMatchService.match(resume, student.getSkills(), internship.getRequiredSkills(), jobDescriptionText);

        model.addAttribute("internship", internship);
        model.addAttribute("result", result);
        model.addAttribute("customJd", jobDescriptionText);
        return "ats-match";
    }
}
