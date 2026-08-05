package com.skillbridge.repository;

import com.skillbridge.entity.RoadmapProgress;
import com.skillbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoadmapProgressRepository extends JpaRepository<RoadmapProgress, Long> {
    List<RoadmapProgress> findByStudent(User student);
    Optional<RoadmapProgress> findByStudentAndStepId(User student, Long stepId);
}
