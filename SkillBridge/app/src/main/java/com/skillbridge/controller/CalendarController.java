package com.skillbridge.controller;

import com.skillbridge.entity.Interview;
import com.skillbridge.entity.User;
import com.skillbridge.service.CalendarService;
import com.skillbridge.service.InterviewService;
import com.skillbridge.service.UserService;
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

import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class CalendarController {

    private final InterviewService interviewService;
    private final UserService userService;
    private final CalendarService calendarService;

    @GetMapping("/interviews/{id}/ics")
    public ResponseEntity<byte[]> downloadIcs(@PathVariable Long id, Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        Interview interview = interviewService.findById(id);

        boolean isOwner = interview.getStudent().getId().equals(user.getId());
        boolean isStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_RECRUITER"));
        if (!isOwner && !isStaff) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        String ics = calendarService.generateIcs(interview);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"interview-" + id + ".ics\"")
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(ics.getBytes(StandardCharsets.UTF_8));
    }
}
