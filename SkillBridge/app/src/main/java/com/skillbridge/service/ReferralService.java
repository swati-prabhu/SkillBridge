package com.skillbridge.service;

import com.skillbridge.entity.*;
import com.skillbridge.repository.ReferralRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralRequestRepository referralRequestRepository;
    private final NotificationService notificationService;

    public List<ReferralRequest> findByStudent(User student) {
        return referralRequestRepository.findByStudentOrderByCreatedAtDesc(student);
    }

    public List<ReferralRequest> findByAlumni(User alumni) {
        return referralRequestRepository.findByAlumniOrderByCreatedAtDesc(alumni);
    }

    public ReferralRequest request(User student, User alumni, Internship internship, String message) {
        ReferralRequest req = new ReferralRequest();
        req.setStudent(student);
        req.setAlumni(alumni);
        req.setInternship(internship);
        req.setMessage(message);
        ReferralRequest saved = referralRequestRepository.save(req);

        notificationService.notify(alumni,
                student.getFullName() + " requested a referral" + (internship != null ? " for \"" + internship.getTitle() + "\"" : "") + ".",
                "/referrals/received");
        return saved;
    }

    public ReferralRequest respond(Long requestId, ReferralStatus status, String responseNote) {
        ReferralRequest req = referralRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Referral request not found: " + requestId));
        req.setStatus(status);
        req.setResponseNote(responseNote);
        req.setRespondedAt(LocalDateTime.now());
        ReferralRequest saved = referralRequestRepository.save(req);

        notificationService.notify(req.getStudent(),
                "Your referral request to " + req.getAlumni().getFullName() + " was " + status.name().toLowerCase() + ".",
                "/referrals");
        return saved;
    }
}
