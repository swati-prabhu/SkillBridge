package com.skillbridge.repository;

import com.skillbridge.entity.Bookmark;
import com.skillbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByStudent(User student);
    Optional<Bookmark> findByStudentAndInternshipId(User student, Long internshipId);
    boolean existsByStudentAndInternshipId(User student, Long internshipId);
}
