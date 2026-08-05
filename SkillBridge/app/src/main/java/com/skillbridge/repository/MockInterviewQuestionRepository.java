package com.skillbridge.repository;

import com.skillbridge.entity.MockInterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MockInterviewQuestionRepository extends JpaRepository<MockInterviewQuestion, Long> {}
