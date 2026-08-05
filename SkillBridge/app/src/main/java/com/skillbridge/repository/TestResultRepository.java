package com.skillbridge.repository;

import com.skillbridge.entity.TestResult;
import com.skillbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findByStudent(User student);
}
