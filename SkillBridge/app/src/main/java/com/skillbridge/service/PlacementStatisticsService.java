package com.skillbridge.service;

import com.skillbridge.dto.PlacementStatistics;
import com.skillbridge.entity.Application;
import com.skillbridge.entity.ApplicationStatus;
import com.skillbridge.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlacementStatisticsService {

    private final ApplicationRepository applicationRepository;

    public PlacementStatistics build() {
        List<Application> selected = applicationRepository.findAll().stream()
                .filter(a -> a.getStatus() == ApplicationStatus.SELECTED)
                .collect(Collectors.toList());

        long totalPlaced = selected.size();

        List<Double> packages = selected.stream()
                .map(Application::getOfferedPackage)
                .filter(p -> p != null)
                .collect(Collectors.toList());

        double avgPackage = packages.isEmpty() ? 0.0
                : Math.round(packages.stream().mapToDouble(Double::doubleValue).average().orElse(0) * 100.0) / 100.0;
        double highestPackage = packages.isEmpty() ? 0.0 : packages.stream().mapToDouble(Double::doubleValue).max().orElse(0);

        Map<String, Long> offersByCompany = selected.stream()
                .collect(Collectors.groupingBy(a -> a.getInternship().getCompany(), Collectors.counting()));

        Map<String, Long> placedByDepartment = selected.stream()
                .filter(a -> a.getStudent().getDepartment() != null && !a.getStudent().getDepartment().isBlank())
                .collect(Collectors.groupingBy(a -> a.getStudent().getDepartment(), Collectors.counting()));

        return new PlacementStatistics(totalPlaced, avgPackage, highestPackage, offersByCompany, placedByDepartment);
    }
}
