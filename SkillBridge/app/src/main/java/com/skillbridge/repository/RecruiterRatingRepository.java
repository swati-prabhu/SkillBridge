package com.skillbridge.repository;

import com.skillbridge.entity.Company;
import com.skillbridge.entity.RecruiterRating;
import com.skillbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RecruiterRatingRepository extends JpaRepository<RecruiterRating, Long> {
    List<RecruiterRating> findByCompany(Company company);
    Optional<RecruiterRating> findByStudentAndCompany(User student, Company company);
}
