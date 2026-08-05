package com.skillbridge.controller;

import com.skillbridge.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/audit-log")
public class AuditLogController {

    private static final int PAGE_SIZE = 25;

    private final AuditService auditService;

    @GetMapping
    public String view(@RequestParam(defaultValue = "0") int page, Model model) {
        var result = auditService.recent(PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("logs", result.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getTotalPages());
        return "admin-audit-log";
    }
}
