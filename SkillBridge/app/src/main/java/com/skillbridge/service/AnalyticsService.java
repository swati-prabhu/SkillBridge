package com.skillbridge.service;

import com.skillbridge.dto.AnalyticsSummary;
import com.skillbridge.entity.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserService userService;
    private final InternshipService internshipService;
    private final ApplicationService applicationService;
    private final AptitudeService aptitudeService;
    private final InterviewService interviewService;
    private final PlacementDriveService placementDriveService;
    private final ResumeService resumeService;

    public AnalyticsSummary buildSummary() {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            byStatus.put(status.name(), applicationService.countByStatus(status));
        }

        return new AnalyticsSummary(
                userService.countStudents(),
                internshipService.count(),
                applicationService.count(),
                interviewService.count(),
                placementDriveService.count(),
                byStatus,
                aptitudeService.averageScorePercent(),
                resumeService.averageScore()
        );
    }
}
