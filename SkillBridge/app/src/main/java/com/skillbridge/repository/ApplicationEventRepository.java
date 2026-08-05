package com.skillbridge.repository;

import com.skillbridge.entity.ApplicationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationEventRepository extends JpaRepository<ApplicationEvent, Long> {
}
