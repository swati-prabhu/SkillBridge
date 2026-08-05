package com.skillbridge.controller;

import com.skillbridge.entity.ForumCategory;
import com.skillbridge.entity.User;
import com.skillbridge.service.ForumService;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/forum")
public class ForumController {

    private final ForumService forumService;
    private final UserService userService;

    @GetMapping
    public String list(@RequestParam(required = false) ForumCategory category, Model model) {
        model.addAttribute("posts", category != null ? forumService.findVisibleByCategory(category) : forumService.findVisible());
        model.addAttribute("categories", ForumCategory.values());
        model.addAttribute("selectedCategory", category);
        return "forum";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("post", forumService.findById(id));
        return "forum-post";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("categories", ForumCategory.values());
        return "forum-new";
    }

    @PostMapping
    public String create(@RequestParam String title, @RequestParam String body,
                          @RequestParam ForumCategory category, @RequestParam(required = false) String companyTag,
                          Authentication auth) {
        User author = userService.getByEmail(auth.getName());
        var post = forumService.createPost(author, title, body, category, companyTag);
        return "redirect:/forum/" + post.getId();
    }

    @PostMapping("/{id}/comments")
    public String comment(@PathVariable Long id, @RequestParam String body, Authentication auth) {
        User author = userService.getByEmail(auth.getName());
        forumService.addComment(author, id, body);
        return "redirect:/forum/" + id;
    }

    @PostMapping("/{id}/flag")
    public String flag(@PathVariable Long id) {
        forumService.flag(id);
        return "redirect:/forum/" + id;
    }

    // ── Admin moderation ──
    @GetMapping("/moderate")
    public String moderate(Model model) {
        model.addAttribute("posts", forumService.findAllForModeration());
        return "admin-forum-moderate";
    }

    @PostMapping("/{id}/moderate")
    public String setHidden(@PathVariable Long id, @RequestParam boolean hidden) {
        forumService.setHidden(id, hidden);
        return "redirect:/forum/moderate";
    }
}
