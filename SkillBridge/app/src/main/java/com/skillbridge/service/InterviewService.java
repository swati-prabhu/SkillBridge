package com.skillbridge.service;

import com.skillbridge.entity.*;
import com.skillbridge.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final ApplicationService applicationService;
    private final EmailService emailService;

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public Interview schedule(Application application, LocalDateTime scheduledAt, String mode, String notes) {
        Interview interview = new Interview();
        interview.setApplication(application);
        interview.setStudent(application.getStudent());
        interview.setScheduledAt(scheduledAt);
        interview.setMode(mode);
        interview.setNotes(notes);
        Interview saved = interviewRepository.save(interview);

        applicationService.updateStatus(application.getId(), ApplicationStatus.INTERVIEW_SCHEDULED,
                "Interview scheduled for " + scheduledAt.format(DISPLAY_FORMAT));

        String html = emailService.interviewScheduledEmail(
                application.getStudent().getFullName(),
                application.getInternship().getTitle(),
                application.getInternship().getCompany(),
                scheduledAt.format(DISPLAY_FORMAT),
                mode
        );
        emailService.send(application.getStudent().getEmail(),
                "Interview scheduled: " + application.getInternship().getTitle(), html);

        saved.setEmailSentAt(LocalDateTime.now());
        interviewRepository.save(saved);

        return saved;
    }

    public List<Interview> findByStudent(User student) {
        return interviewRepository.findByStudentOrderByScheduledAtDesc(student);
    }

    public Interview findById(Long id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found: " + id));
    }

    public long count() {
        return interviewRepository.count();
    }
}
