package com.skillbridge.repository;

import com.skillbridge.entity.Application;
import com.skillbridge.entity.ApplicationStatus;
import com.skillbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByStudent(User student);
    List<Application> findByInternshipId(Long internshipId);
    List<Application> findByInternshipIdIn(List<Long> internshipIds);
    long countByStatus(ApplicationStatus status);
    boolean existsByStudentAndInternshipId(User student, Long internshipId);
}
