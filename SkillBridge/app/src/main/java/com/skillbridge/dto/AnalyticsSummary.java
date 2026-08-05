package com.skillbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class AnalyticsSummary {
    private long totalStudents;
    private long totalInternships;
    private long totalApplications;
    private long totalInterviews;
    private long totalPlacementDrives;
    private Map<String, Long> applicationsByStatus;
    private double averageAptitudeScorePercent;
    private double averageResumeScore;
}
