package com.skillbridge.controller;

import com.skillbridge.service.PlacementStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PlacementStatisticsController {

    private final PlacementStatisticsService placementStatisticsService;

    @GetMapping("/placement-statistics")
    public String view(Model model) {
        model.addAttribute("stats", placementStatisticsService.build());
        return "placement-statistics";
    }
}
