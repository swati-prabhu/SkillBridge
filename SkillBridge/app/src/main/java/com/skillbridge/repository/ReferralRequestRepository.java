package com.skillbridge.repository;

import com.skillbridge.entity.ReferralRequest;
import com.skillbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReferralRequestRepository extends JpaRepository<ReferralRequest, Long> {
    List<ReferralRequest> findByStudentOrderByCreatedAtDesc(User student);
    List<ReferralRequest> findByAlumniOrderByCreatedAtDesc(User alumni);
}
