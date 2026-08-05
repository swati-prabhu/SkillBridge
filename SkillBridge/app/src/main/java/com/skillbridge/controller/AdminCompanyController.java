package com.skillbridge.controller;

import com.skillbridge.service.AuditService;
import com.skillbridge.service.CompanyService;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/companies")
public class AdminCompanyController {

    private final CompanyService companyService;
    private final UserService userService;
    private final AuditService auditService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("companies", companyService.findAll());
        model.addAttribute("recruiters", userService.findByRole(com.skillbridge.entity.Role.RECRUITER));
        return "admin-companies";
    }

    @PostMapping("/{id}/verify")
    public String verifyCompany(@PathVariable Long id, @RequestParam boolean verified, Authentication auth) {
        companyService.setVerified(id, verified);
        auditService.log(userService.getByEmail(auth.getName()), verified ? "VERIFY" : "UNVERIFY", "Company", String.valueOf(id), null);
        return "redirect:/admin/companies";
    }

    @PostMapping("/recruiters/{userId}/verify")
    public String verifyRecruiter(@PathVariable Long userId, @RequestParam boolean verified, Authentication auth) {
        var recruiter = userService.findById(userId);
        recruiter.setRecruiterVerified(verified);
        userService.save(recruiter);
        auditService.log(userService.getByEmail(auth.getName()), verified ? "VERIFY_RECRUITER" : "UNVERIFY_RECRUITER", "User", String.valueOf(userId), null);
        return "redirect:/admin/companies";
    }
}
