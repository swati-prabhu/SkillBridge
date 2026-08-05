package com.skillbridge.repository;

import com.skillbridge.entity.Interview;
import com.skillbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByStudentOrderByScheduledAtDesc(User student);
}
