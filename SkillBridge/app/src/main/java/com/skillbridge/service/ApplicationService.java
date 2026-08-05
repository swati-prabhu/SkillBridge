package com.skillbridge.service;

import com.skillbridge.entity.*;
import com.skillbridge.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final InternshipService internshipService;
    private final NotificationService notificationService;
    private final RealtimeBroadcastService realtimeBroadcastService;

    public Application apply(User student, Long internshipId) {
        if (applicationRepository.existsByStudentAndInternshipId(student, internshipId)) {
            throw new IllegalStateException("You have already applied to this internship.");
        }
        Internship internship = internshipService.findById(internshipId);
        Application application = new Application();
        application.setStudent(student);
        application.setInternship(internship);
        application.setStatus(ApplicationStatus.APPLIED);

        ApplicationEvent event = new ApplicationEvent();
        event.setApplication(application);
        event.setStatus(ApplicationStatus.APPLIED);
        event.setNote("Application submitted");
        application.getTimeline().add(event);

        Application saved = applicationRepository.save(application);
        realtimeBroadcastService.broadcastStatusChange(saved);
        return saved;
    }

    public List<Application> findByStudent(User student) {
        return applicationRepository.findByStudent(student);
    }

    public List<Application> findAll() {
        return applicationRepository.findAll();
    }

    public List<Application> findByInternshipIds(List<Long> internshipIds) {
        if (internshipIds.isEmpty()) return List.of();
        return applicationRepository.findByInternshipIdIn(internshipIds);
    }

    public Application findById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
    }

    public Application updateStatus(Long applicationId, ApplicationStatus status) {
        return updateStatus(applicationId, status, null, null);
    }

    public Application updateStatus(Long applicationId, ApplicationStatus status, String note) {
        return updateStatus(applicationId, status, note, null);
    }

    public Application updateStatus(Long applicationId, ApplicationStatus status, String note, Double offeredPackage) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        application.setStatus(status);
        if (offeredPackage != null) {
            application.setOfferedPackage(offeredPackage);
        }

        ApplicationEvent event = new ApplicationEvent();
        event.setApplication(application);
        event.setStatus(status);
        event.setNote(note);
        application.getTimeline().add(event);

        Application saved = applicationRepository.save(application);

        String message = "Your application for \"" + application.getInternship().getTitle()
                + "\" at " + application.getInternship().getCompany() + " is now "
                + status.name().replace("_", " ") + ".";
        notificationService.notify(application.getStudent(), message, "/applications/my");
        realtimeBroadcastService.broadcastStatusChange(saved);

        return saved;
    }

    public long count() {
        return applicationRepository.count();
    }

    public long countByStatus(ApplicationStatus status) {
        return applicationRepository.countByStatus(status);
    }
}
