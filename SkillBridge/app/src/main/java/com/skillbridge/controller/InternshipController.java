package com.skillbridge.controller;

import com.skillbridge.entity.Internship;
import com.skillbridge.entity.Role;
import com.skillbridge.entity.User;
import com.skillbridge.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
@RequiredArgsConstructor
@RequestMapping("/internships")
public class InternshipController {

    private static final int PAGE_SIZE = 9;

    private final InternshipService internshipService;
    private final BookmarkService bookmarkService;
    private final UserService userService;
    private final AuditService auditService;
    private final SavedSearchService savedSearchService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String category,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Page<Internship> result = internshipService.search(keyword, category, PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("internships", result.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("categories", internshipService.findDistinctCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getTotalPages());
        return "internships";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Authentication auth, Model model) {
        Internship internship = internshipService.findById(id);
        model.addAttribute("internship", internship);

        boolean isStudent = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
        if (isStudent) {
            User student = userService.getByEmail(auth.getName());
            model.addAttribute("bookmarked", bookmarkService.isBookmarked(student, id));
        }
        return "internship-detail";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("internship", new Internship());
        return "internship-form";
    }

    @PostMapping
    public String save(@ModelAttribute Internship submitted, Authentication auth) {
        User actor = userService.getByEmail(auth.getName());
        boolean isNew = submitted.getId() == null;

        if (!isNew) {
            // Editing an existing internship - enforce ownership for recruiters before anything is persisted.
            Internship existing = internshipService.findById(submitted.getId());
            if (actor.getRole() == Role.RECRUITER
                    && (existing.getPostedBy() == null || !existing.getPostedBy().getId().equals(actor.getId()))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit internships you posted.");
            }
            submitted.setPostedBy(existing.getPostedBy());
            submitted.setCompanyRef(existing.getCompanyRef());
            if (actor.getRole() == Role.RECRUITER) {
                submitted.setCompany(actor.getCompany().getName()); // never trust the client for this - always derive from the account
            }
        } else {
            submitted.setPostedBy(actor);
            if (actor.getRole() == Role.RECRUITER) {
                submitted.setCompanyRef(actor.getCompany());
                submitted.setCompany(actor.getCompany().getName());
            }
        }

        Internship saved = internshipService.save(submitted);
        auditService.log(actor, isNew ? "CREATE" : "UPDATE", "Internship", String.valueOf(saved.getId()), saved.getTitle());

        if (isNew) {
            internshipService.notifyMatchingStudents(saved);
            savedSearchService.notifyMatchingSavedSearches(saved);
        }

        return isNew ? "redirect:/internships" : "redirect:/internships/" + saved.getId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication auth, Model model) {
        Internship internship = internshipService.findById(id);
        User actor = userService.getByEmail(auth.getName());
        if (actor.getRole() == Role.RECRUITER
                && (internship.getPostedBy() == null || !internship.getPostedBy().getId().equals(actor.getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit internships you posted.");
        }
        model.addAttribute("internship", internship);
        return "internship-form";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth) {
        Internship internship = internshipService.findById(id);
        User actor = userService.getByEmail(auth.getName());
        if (actor.getRole() == Role.RECRUITER
                && (internship.getPostedBy() == null || !internship.getPostedBy().getId().equals(actor.getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete internships you posted.");
        }
        auditService.log(actor, "DELETE", "Internship", String.valueOf(id), internship.getTitle());
        internshipService.delete(id);
        return "redirect:/internships";
    }
}
