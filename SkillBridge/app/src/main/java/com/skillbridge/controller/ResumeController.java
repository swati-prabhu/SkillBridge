package com.skillbridge.controller;

import com.skillbridge.entity.*;
import com.skillbridge.service.AiResumeReviewService;
import com.skillbridge.service.ResumePdfService;
import com.skillbridge.service.ResumeScoreService;
import com.skillbridge.service.ResumeService;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/resume")
public class ResumeController {

    private final ResumeService resumeService;
    private final UserService userService;
    private final ResumePdfService resumePdfService;
    private final ResumeScoreService resumeScoreService;
    private final AiResumeReviewService aiResumeReviewService;

    @GetMapping
    public String builder(Authentication auth, Model model) {
        User user = userService.getByEmail(auth.getName());
        Resume resume = resumeService.findOrCreate(user);
        model.addAttribute("resume", resume);
        model.addAttribute("user", user);
        model.addAttribute("scoreLabel", resumeScoreService.label(resume.getScore()));
        if (resume.getId() != null) {
            model.addAttribute("review", aiResumeReviewService.review(resume, user.getSkills(), resume.getScore()));
        }
        return "resume-builder";
    }

    @PostMapping
    public String save(Authentication auth,
                        @RequestParam(required = false) String headline,
                        @RequestParam(required = false) String summary,
                        @RequestParam(required = false) String github,
                        @RequestParam(required = false) String linkedin,
                        @RequestParam(required = false) String portfolio,
                        @RequestParam(required = false) String skills,
                        @RequestParam(required = false) List<String> eduInstitution,
                        @RequestParam(required = false) List<String> eduDegree,
                        @RequestParam(required = false) List<String> eduStartYear,
                        @RequestParam(required = false) List<String> eduEndYear,
                        @RequestParam(required = false) List<String> expCompany,
                        @RequestParam(required = false) List<String> expRole,
                        @RequestParam(required = false) List<String> expStartDate,
                        @RequestParam(required = false) List<String> expEndDate,
                        @RequestParam(required = false) List<String> expDescription,
                        @RequestParam(required = false) List<String> projName,
                        @RequestParam(required = false) List<String> projDescription,
                        @RequestParam(required = false) List<String> projLink,
                        @RequestParam(required = false) List<String> projTechStack) {

        User user = userService.getByEmail(auth.getName());

        Resume incoming = new Resume();
        incoming.setHeadline(headline);
        incoming.setSummary(summary);
        incoming.setGithub(github);
        incoming.setLinkedin(linkedin);
        incoming.setPortfolio(portfolio);

        List<ResumeEducation> education = new ArrayList<>();
        if (eduInstitution != null) {
            for (int i = 0; i < eduInstitution.size(); i++) {
                if (eduInstitution.get(i) == null || eduInstitution.get(i).isBlank()) continue;
                ResumeEducation e = new ResumeEducation();
                e.setInstitution(eduInstitution.get(i));
                e.setDegree(get(eduDegree, i));
                e.setStartYear(get(eduStartYear, i));
                e.setEndYear(get(eduEndYear, i));
                education.add(e);
            }
        }

        List<ResumeExperience> experience = new ArrayList<>();
        if (expCompany != null) {
            for (int i = 0; i < expCompany.size(); i++) {
                if (expCompany.get(i) == null || expCompany.get(i).isBlank()) continue;
                ResumeExperience e = new ResumeExperience();
                e.setCompany(expCompany.get(i));
                e.setRole(get(expRole, i));
                e.setStartDate(get(expStartDate, i));
                e.setEndDate(get(expEndDate, i));
                e.setDescription(get(expDescription, i));
                experience.add(e);
            }
        }

        List<ResumeProject> projects = new ArrayList<>();
        if (projName != null) {
            for (int i = 0; i < projName.size(); i++) {
                if (projName.get(i) == null || projName.get(i).isBlank()) continue;
                ResumeProject p = new ResumeProject();
                p.setName(projName.get(i));
                p.setDescription(get(projDescription, i));
                p.setLink(get(projLink, i));
                p.setTechStack(get(projTechStack, i));
                projects.add(p);
            }
        }

        resumeService.save(user, incoming, education, experience, projects, skills);
        return "redirect:/resume";
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadPdf(Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        Resume resume = resumeService.findOrCreate(user);
        byte[] pdf = resumePdfService.generate(user, resume);

        String filename = user.getFullName().replaceAll("\\s+", "_") + "_Resume.pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private String get(List<String> list, int index) {
        return list != null && index < list.size() ? list.get(index) : null;
    }
}
