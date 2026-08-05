package com.skillbridge.controller;

import com.skillbridge.entity.User;
import com.skillbridge.service.InternshipService;
import com.skillbridge.service.SavedSearchService;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/saved-searches")
public class SavedSearchController {

    private final SavedSearchService savedSearchService;
    private final UserService userService;
    private final InternshipService internshipService;

    @GetMapping
    public String list(Authentication auth, Model model) {
        User student = userService.getByEmail(auth.getName());
        model.addAttribute("savedSearches", savedSearchService.findByStudent(student));
        model.addAttribute("categories", internshipService.findDistinctCategories());
        return "saved-searches";
    }

    @PostMapping
    public String create(@RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String category,
                          Authentication auth) {
        User student = userService.getByEmail(auth.getName());
        savedSearchService.save(student, keyword, category);
        return "redirect:/saved-searches";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        savedSearchService.delete(id);
        return "redirect:/saved-searches";
    }
}
