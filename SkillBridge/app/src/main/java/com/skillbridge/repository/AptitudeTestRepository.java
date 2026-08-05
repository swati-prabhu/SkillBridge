package com.skillbridge.repository;

import com.skillbridge.entity.AptitudeTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AptitudeTestRepository extends JpaRepository<AptitudeTest, Long> {
    List<AptitudeTest> findByContestFalse();
    List<AptitudeTest> findByContestTrue();
}
