package com.skillbridge.repository;

import com.skillbridge.entity.SavedSearch;
import com.skillbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavedSearchRepository extends JpaRepository<SavedSearch, Long> {
    List<SavedSearch> findByStudent(User student);
}
