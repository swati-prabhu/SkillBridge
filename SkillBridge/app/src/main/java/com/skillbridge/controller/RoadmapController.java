package com.skillbridge.controller;

import com.skillbridge.entity.Roadmap;
import com.skillbridge.entity.User;
import com.skillbridge.service.RoadmapService;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final UserService userService;

    @GetMapping
    public String list(Authentication auth, Model model) {
        User student = userService.getByEmail(auth.getName());
        var roadmaps = roadmapService.findAll();

        Map<Long, Integer> percentByRoadmap = new HashMap<>();
        for (Roadmap r : roadmaps) {
            percentByRoadmap.put(r.getId(), roadmapService.percentComplete(student, r));
        }

        model.addAttribute("roadmaps", roadmaps);
        model.addAttribute("percentByRoadmap", percentByRoadmap);
        model.addAttribute("recommendedRoadmapId",
                roadmapService.recommendForSkills(student.getSkills()).map(Roadmap::getId).orElse(null));
        return "roadmaps";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Authentication auth, Model model) {
        User student = userService.getByEmail(auth.getName());
        Roadmap roadmap = roadmapService.findById(id);

        model.addAttribute("roadmap", roadmap);
        model.addAttribute("completion", roadmapService.completionMap(student, roadmap));
        model.addAttribute("percent", roadmapService.percentComplete(student, roadmap));
        return "roadmap-detail";
    }

    @PostMapping("/steps/{stepId}/toggle")
    public String toggleStep(@PathVariable Long stepId, @RequestParam Long roadmapId, Authentication auth) {
        User student = userService.getByEmail(auth.getName());
        roadmapService.toggleStep(student, stepId);
        return "redirect:/roadmaps/" + roadmapId;
    }
}
