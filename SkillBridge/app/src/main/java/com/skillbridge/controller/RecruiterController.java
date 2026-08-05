package com.skillbridge.controller;

import com.skillbridge.entity.*;
import com.skillbridge.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/recruiter")
public class RecruiterController {

    private final UserService userService;
    private final InternshipService internshipService;
    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final AuditService auditService;
    private final RecruiterRatingService recruiterRatingService;
    private final CompanyService companyService;

    @GetMapping("/internships")
    public String myInternships(Authentication auth, Model model) {
        User recruiter = userService.getByEmail(auth.getName());
        List<Internship> internships = internshipService.findByPostedBy(recruiter.getId());
        model.addAttribute("internships", internships);
        return "recruiter-internships";
    }

    @GetMapping("/internships/{id}/applicants")
    public String applicants(@PathVariable Long id, Authentication auth, Model model) {
        User recruiter = userService.getByEmail(auth.getName());
        Internship internship = internshipService.findById(id);
        requireOwnership(internship, recruiter);

        List<Application> applications = applicationService.findByInternshipIds(List.of(id));
        model.addAttribute("internship", internship);
        model.addAttribute("applications", applications);
        model.addAttribute("statuses", ApplicationStatus.values());
        return "recruiter-applicants";
    }

    @PostMapping("/applications/{id}/status")
    public String updateStatus(@PathVariable Long id,
                                @RequestParam ApplicationStatus status,
                                @RequestParam(required = false) Double offeredPackage,
                                Authentication auth) {
        User recruiter = userService.getByEmail(auth.getName());
        Application application = applicationService.findById(id);
        requireOwnership(application.getInternship(), recruiter);

        applicationService.updateStatus(id, status, null, status == ApplicationStatus.SELECTED ? offeredPackage : null);
        auditService.log(recruiter, "UPDATE_STATUS", "Application", String.valueOf(id), status.name());
        return "redirect:/recruiter/internships/" + application.getInternship().getId() + "/applicants";
    }

    @PostMapping("/applications/{id}/schedule-interview")
    public String scheduleInterview(@PathVariable Long id,
                                     @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime scheduledAt,
                                     @RequestParam String mode,
                                     @RequestParam(required = false) String notes,
                                     Authentication auth) {
        User recruiter = userService.getByEmail(auth.getName());
        Application application = applicationService.findById(id);
        requireOwnership(application.getInternship(), recruiter);

        interviewService.schedule(application, scheduledAt, mode, notes);
        auditService.log(recruiter, "SCHEDULE_INTERVIEW", "Application", String.valueOf(id), scheduledAt.toString());
        return "redirect:/recruiter/internships/" + application.getInternship().getId() + "/applicants";
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        User recruiter = userService.getByEmail(auth.getName());
        model.addAttribute("user", recruiter);
        model.addAttribute("company", recruiter.getCompany());
        if (recruiter.getCompany() != null) {
            model.addAttribute("ratingSummary", recruiterRatingService.summarize(recruiter.getCompany()));
        }
        return "recruiter-profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String designation,
                                 @RequestParam(required = false) String companyDescription,
                                 @RequestParam(required = false) String companyWebsite,
                                 Authentication auth) {
        User recruiter = userService.getByEmail(auth.getName());
        recruiter.setDesignation(designation);
        userService.save(recruiter);

        if (recruiter.getCompany() != null) {
            Company company = recruiter.getCompany();
            company.setDescription(companyDescription);
            company.setWebsite(companyWebsite);
            companyService.save(company);
        }
        return "redirect:/recruiter/profile";
    }

    private void requireOwnership(Internship internship, User recruiter) {
        if (internship.getPostedBy() == null || !internship.getPostedBy().getId().equals(recruiter.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage applicants for internships you posted.");
        }
    }
}
