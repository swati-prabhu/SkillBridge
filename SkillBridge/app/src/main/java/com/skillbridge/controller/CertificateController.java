package com.skillbridge.controller;

import com.skillbridge.entity.*;
import com.skillbridge.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final UserService userService;
    private final AptitudeService aptitudeService;
    private final RoadmapService roadmapService;
    private final MockInterviewService mockInterviewService;

    @GetMapping("/certificates/test/{resultId}")
    public ResponseEntity<byte[]> testCertificate(@PathVariable Long resultId, Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        TestResult result = aptitudeService.findResultById(resultId);
        requireOwner(result.getStudent(), user);

        int percent = result.getTotalQuestions() == 0 ? 0 : Math.round(100f * result.getScore() / result.getTotalQuestions());
        byte[] pdf = certificateService.generate(user, "\"" + result.getTest().getTitle() + "\"",
                "Score: " + result.getScore() + "/" + result.getTotalQuestions() + " (" + percent + "%)");
        return pdfResponse(pdf, "certificate-test-" + resultId + ".pdf");
    }

    @GetMapping("/certificates/roadmap/{roadmapId}")
    public ResponseEntity<byte[]> roadmapCertificate(@PathVariable Long roadmapId, Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        Roadmap roadmap = roadmapService.findById(roadmapId);
        int percent = roadmapService.percentComplete(user, roadmap);
        if (percent < 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Complete all roadmap steps first.");
        }
        byte[] pdf = certificateService.generate(user, "the \"" + roadmap.getTitle() + "\" roadmap", null);
        return pdfResponse(pdf, "certificate-roadmap-" + roadmapId + ".pdf");
    }

    @GetMapping("/certificates/mock-interview/{attemptId}")
    public ResponseEntity<byte[]> mockInterviewCertificate(@PathVariable Long attemptId, Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        MockInterviewAttempt attempt = mockInterviewService.findAttemptById(attemptId);
        requireOwner(attempt.getStudent(), user);
        if (!attempt.isReviewed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This attempt hasn't been reviewed yet.");
        }
        byte[] pdf = certificateService.generate(user, "the \"" + attempt.getMockInterview().getTitle() + "\" mock interview",
                attempt.getOverallScore() != null ? "Score: " + attempt.getOverallScore() + "%" : null);
        return pdfResponse(pdf, "certificate-mock-interview-" + attemptId + ".pdf");
    }

    private void requireOwner(User owner, User actor) {
        if (!owner.getId().equals(actor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
