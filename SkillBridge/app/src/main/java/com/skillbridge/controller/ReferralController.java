package com.skillbridge.controller;

import com.skillbridge.entity.*;
import com.skillbridge.service.InternshipService;
import com.skillbridge.service.ReferralService;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/referrals")
public class ReferralController {

    private final ReferralService referralService;
    private final UserService userService;
    private final InternshipService internshipService;

    @GetMapping
    public String myRequests(Authentication auth, Model model) {
        User student = userService.getByEmail(auth.getName());
        model.addAttribute("requests", referralService.findByStudent(student));
        model.addAttribute("alumni", userService.findAllAlumni());
        model.addAttribute("internships", internshipService.findAll());
        return "referrals";
    }

    @PostMapping
    public String request(@RequestParam Long alumniId, @RequestParam(required = false) Long internshipId,
                           @RequestParam(required = false) String message, Authentication auth) {
        User student = userService.getByEmail(auth.getName());
        User alumni = userService.findById(alumniId);
        Internship internship = internshipId != null ? internshipService.findById(internshipId) : null;
        referralService.request(student, alumni, internship, message);
        return "redirect:/referrals";
    }

    @GetMapping("/received")
    public String received(Authentication auth, Model model) {
        User alumni = userService.getByEmail(auth.getName());
        model.addAttribute("requests", referralService.findByAlumni(alumni));
        model.addAttribute("statuses", ReferralStatus.values());
        return "referrals-received";
    }

    @PostMapping("/{id}/respond")
    public String respond(@PathVariable Long id, @RequestParam ReferralStatus status,
                           @RequestParam(required = false) String responseNote) {
        referralService.respond(id, status, responseNote);
        return "redirect:/referrals/received";
    }
}
