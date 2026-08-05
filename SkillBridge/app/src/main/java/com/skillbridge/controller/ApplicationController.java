package com.skillbridge.controller;

import com.skillbridge.entity.Application;
import com.skillbridge.entity.ApplicationStatus;
import com.skillbridge.entity.User;
import com.skillbridge.service.ApplicationService;
import com.skillbridge.service.AuditService;
import com.skillbridge.service.InterviewService;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserService userService;
    private final InterviewService interviewService;
    private final AuditService auditService;

    @PostMapping("/apply/{internshipId}")
    public String apply(@PathVariable Long internshipId, Authentication auth, Model model) {
        User student = userService.getByEmail(auth.getName());
        try {
            applicationService.apply(student, internshipId);
        } catch (IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/my")
    public String myApplications(Authentication auth, Model model) {
        User student = userService.getByEmail(auth.getName());
        model.addAttribute("applications", applicationService.findByStudent(student));
        return "applications";
    }

    @GetMapping("/manage")
    public String manage(Model model) {
        model.addAttribute("applications", applicationService.findAll());
        model.addAttribute("statuses", ApplicationStatus.values());
        return "applications-manage";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam ApplicationStatus status,
                                @RequestParam(required = false) Double offeredPackage, Authentication auth) {
        applicationService.updateStatus(id, status, null, status == ApplicationStatus.SELECTED ? offeredPackage : null);
        auditService.log(userService.getByEmail(auth.getName()), "UPDATE_STATUS", "Application", String.valueOf(id), status.name());
        return "redirect:/applications/manage";
    }

    @PostMapping("/{id}/schedule-interview")
    public String scheduleInterview(@PathVariable Long id,
                                     @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime scheduledAt,
                                     @RequestParam String mode,
                                     @RequestParam(required = false) String notes,
                                     Authentication auth) {
        Application application = applicationService.findById(id);
        interviewService.schedule(application, scheduledAt, mode, notes);
        auditService.log(userService.getByEmail(auth.getName()), "SCHEDULE_INTERVIEW", "Application", String.valueOf(id), scheduledAt.toString());
        return "redirect:/applications/manage";
    }
}
