package com.skillbridge.repository;

import com.skillbridge.entity.ForumCategory;
import com.skillbridge.entity.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    List<ForumPost> findByHiddenFalseOrderByCreatedAtDesc();
    List<ForumPost> findByHiddenFalseAndCategoryOrderByCreatedAtDesc(ForumCategory category);
    List<ForumPost> findAllByOrderByCreatedAtDesc();
}
