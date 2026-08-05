package com.skillbridge.controller;

import com.skillbridge.entity.User;
import com.skillbridge.service.BookmarkService;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final UserService userService;

    @GetMapping
    public String myBookmarks(Authentication auth, Model model) {
        User student = userService.getByEmail(auth.getName());
        model.addAttribute("bookmarks", bookmarkService.findByStudent(student));
        return "bookmarks";
    }

    @PostMapping("/toggle/{internshipId}")
    public String toggle(@PathVariable Long internshipId, Authentication auth) {
        User student = userService.getByEmail(auth.getName());
        bookmarkService.toggle(student, internshipId);
        return "redirect:/internships/" + internshipId;
    }
}
