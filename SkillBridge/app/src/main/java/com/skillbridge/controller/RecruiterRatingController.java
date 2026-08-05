package com.skillbridge.controller;

import com.skillbridge.entity.Company;
import com.skillbridge.entity.User;
import com.skillbridge.service.CompanyService;
import com.skillbridge.service.RecruiterRatingService;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/companies")
public class RecruiterRatingController {

    private final CompanyService companyService;
    private final RecruiterRatingService recruiterRatingService;
    private final UserService userService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("companies", companyService.findAll());
        return "companies";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Authentication auth, Model model) {
        Company company = companyService.findById(id);
        User student = userService.getByEmail(auth.getName());
        model.addAttribute("company", company);
        model.addAttribute("ratingSummary", recruiterRatingService.summarize(company));
        model.addAttribute("alreadyRated", recruiterRatingService.hasRated(student, company));
        return "company-detail";
    }

    @PostMapping("/{id}/rate")
    public String rate(@PathVariable Long id,
                        @RequestParam int difficulty, @RequestParam int communication, @RequestParam int process,
                        @RequestParam(required = false) String comment,
                        Authentication auth, Model model) {
        Company company = companyService.findById(id);
        User student = userService.getByEmail(auth.getName());
        try {
            recruiterRatingService.submit(student, company, difficulty, communication, process, comment);
        } catch (IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "redirect:/companies/" + id;
    }
}
