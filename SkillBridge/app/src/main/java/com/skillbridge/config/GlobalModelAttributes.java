package com.skillbridge.config;

import com.skillbridge.entity.User;
import com.skillbridge.service.NotificationService;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Runs before every @Controller request and injects notification data so the
 * navbar bell icon works on every page without each controller adding it manually.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final NotificationService notificationService;
    private final UserService userService;

    @ModelAttribute("unreadNotifications")
    public long unreadNotifications(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return 0;
        }
        try {
            User user = userService.getByEmail(auth.getName());
            return notificationService.unreadCount(user);
        } catch (Exception e) {
            return 0;
        }
    }

    @ModelAttribute("recentNotifications")
    public Object recentNotifications(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return java.util.List.of();
        }
        try {
            User user = userService.getByEmail(auth.getName());
            var all = notificationService.recentForUser(user);
            return all.size() > 5 ? all.subList(0, 5) : all;
        } catch (Exception e) {
            return java.util.List.of();
        }
    }
}
