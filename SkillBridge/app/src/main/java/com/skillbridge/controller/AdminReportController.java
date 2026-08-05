package com.skillbridge.controller;

import com.skillbridge.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reports")
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping
    public String index() {
        return "admin-reports";
    }

    @GetMapping("/placements.pdf")
    public ResponseEntity<byte[]> placements() {
        return pdf(adminReportService.placementsReport(), "placements-report.pdf");
    }

    @GetMapping("/interviews.pdf")
    public ResponseEntity<byte[]> interviews() {
        return pdf(adminReportService.interviewsReport(), "interviews-report.pdf");
    }

    @GetMapping("/student-progress.pdf")
    public ResponseEntity<byte[]> studentProgress() {
        return pdf(adminReportService.studentProgressReport(), "student-progress-report.pdf");
    }

    private ResponseEntity<byte[]> pdf(byte[] bytes, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
