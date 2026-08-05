package com.skillbridge.repository;

import com.skillbridge.entity.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {}
