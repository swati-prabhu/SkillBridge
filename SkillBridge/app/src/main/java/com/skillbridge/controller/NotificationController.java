package com.skillbridge.controller;

import com.skillbridge.entity.User;
import com.skillbridge.service.NotificationService;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @PostMapping("/mark-read")
    public String markRead(Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        notificationService.markAllRead(user);
        return "redirect:/dashboard";
    }
}
