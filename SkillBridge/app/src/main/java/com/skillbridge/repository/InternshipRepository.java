package com.skillbridge.repository;

import com.skillbridge.entity.Internship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InternshipRepository extends JpaRepository<Internship, Long> {

    List<Internship> findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCaseOrLocationContainingIgnoreCase(
            String title, String company, String location);

    @Query("""
           SELECT i FROM Internship i
           WHERE (:keyword IS NULL OR :keyword = '' OR
                  LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                  LOWER(i.company) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                  LOWER(i.location) LIKE LOWER(CONCAT('%', :keyword, '%')))
             AND (:category IS NULL OR :category = '' OR i.category = :category)
           ORDER BY i.createdAt DESC
           """)
    Page<Internship> search(@Param("keyword") String keyword, @Param("category") String category, Pageable pageable);

    @Query("SELECT DISTINCT i.category FROM Internship i ORDER BY i.category")
    List<String> findDistinctCategories();

    List<Internship> findByPostedById(Long postedById);
}
