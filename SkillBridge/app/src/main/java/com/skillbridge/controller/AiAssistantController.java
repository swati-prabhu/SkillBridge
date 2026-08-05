package com.skillbridge.controller;

import com.skillbridge.entity.User;
import com.skillbridge.service.AiAssistantService;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assistant")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;
    private final UserService userService;

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> body, Authentication auth) {
        User student = userService.getByEmail(auth.getName());
        String reply = aiAssistantService.respond(student, body.get("message"));
        return Map.of("reply", reply);
    }
}
