package com.skillbridge.service;

import com.skillbridge.entity.PlacementDrive;
import com.skillbridge.repository.PlacementDriveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlacementDriveService {

    private final PlacementDriveRepository placementDriveRepository;

    public List<PlacementDrive> findAll() {
        return placementDriveRepository.findAll();
    }

    public PlacementDrive findById(Long id) {
        return placementDriveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Placement drive not found: " + id));
    }

    public PlacementDrive save(PlacementDrive drive) {
        return placementDriveRepository.save(drive);
    }

    public void setActive(Long id, boolean active) {
        PlacementDrive drive = findById(id);
        drive.setActive(active);
        placementDriveRepository.save(drive);
    }

    public long count() {
        return placementDriveRepository.count();
    }
}
