package com.skillbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class PlacementStatistics {
    private long totalPlacedStudents;
    private double averagePackage;
    private double highestPackage;
    private Map<String, Long> offersByCompany;
    private Map<String, Long> placedByDepartment;
}
