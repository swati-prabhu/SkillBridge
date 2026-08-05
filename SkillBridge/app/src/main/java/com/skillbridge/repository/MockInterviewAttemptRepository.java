package com.skillbridge.repository;

import com.skillbridge.entity.MockInterviewAttempt;
import com.skillbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MockInterviewAttemptRepository extends JpaRepository<MockInterviewAttempt, Long> {
    List<MockInterviewAttempt> findByStudentOrderBySubmittedAtDesc(User student);
    List<MockInterviewAttempt> findByReviewedFalseOrderBySubmittedAtAsc();
}
