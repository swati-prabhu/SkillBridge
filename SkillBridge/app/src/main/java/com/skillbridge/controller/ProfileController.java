package com.skillbridge.controller;

import com.skillbridge.entity.User;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        model.addAttribute("user", userService.getByEmail(auth.getName()));
        return "profile";
    }

    @PostMapping("/profile/skills")
    public String updateSkills(@RequestParam String skills, Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        user.setSkills(skills);
        userService.save(user);
        return "redirect:/profile";
    }

    @PostMapping("/profile/resume")
    public String uploadResume(@RequestParam("resume") MultipartFile file, Authentication auth, Model model) throws IOException {
        User user = userService.getByEmail(auth.getName());

        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        String filename = "resume_" + user.getId() + "_" + file.getOriginalFilename();
        Path target = dir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        user.setResumePath(target.toString());
        userService.save(user);
        return "redirect:/profile";
    }
}
