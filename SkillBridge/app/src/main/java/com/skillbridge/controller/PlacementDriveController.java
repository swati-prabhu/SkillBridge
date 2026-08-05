package com.skillbridge.controller;

import com.skillbridge.entity.PlacementDrive;
import com.skillbridge.entity.User;
import com.skillbridge.service.AuditService;
import com.skillbridge.service.PlacementDriveService;
import com.skillbridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/drives")
public class PlacementDriveController {

    private final PlacementDriveService placementDriveService;
    private final UserService userService;
    private final AuditService auditService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("drives", placementDriveService.findAll());
        model.addAttribute("newDrive", new PlacementDrive());
        return "admin-drives";
    }

    @PostMapping
    public String create(@RequestParam String name,
                          @RequestParam String company,
                          @RequestParam String description,
                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate driveDate,
                          Authentication auth) {
        PlacementDrive drive = new PlacementDrive();
        drive.setName(name);
        drive.setCompany(company);
        drive.setDescription(description);
        drive.setDriveDate(driveDate);
        PlacementDrive saved = placementDriveService.save(drive);

        auditService.log(userService.getByEmail(auth.getName()), "CREATE", "PlacementDrive", String.valueOf(saved.getId()), saved.getName());
        return "redirect:/admin/drives";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, @RequestParam boolean active, Authentication auth) {
        placementDriveService.setActive(id, active);
        auditService.log(userService.getByEmail(auth.getName()), active ? "ACTIVATE" : "DEACTIVATE", "PlacementDrive", String.valueOf(id), null);
        return "redirect:/admin/drives";
    }
}
