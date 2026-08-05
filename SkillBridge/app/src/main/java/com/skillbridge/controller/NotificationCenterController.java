package com.skillbridge.controller;

import com.skillbridge.entity.User;
import com.skillbridge.service.NotificationService;
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
@RequestMapping("/notifications")
public class NotificationCenterController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    public String center(Authentication auth, Model model) {
        User user = userService.getByEmail(auth.getName());
        model.addAttribute("notifications", notificationService.recentForUser(user));
        return "notification-center";
    }

    @PostMapping("/mark-all-read")
    public String markAllRead(Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        notificationService.markAllRead(user);
        return "redirect:/notifications";
    }

    @PostMapping("/{id}/mark-read")
    public String markOneRead(@PathVariable Long id, Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        notificationService.markRead(user, id);
        return "redirect:/notifications";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        notificationService.delete(user, id);
        return "redirect:/notifications";
    }
}
