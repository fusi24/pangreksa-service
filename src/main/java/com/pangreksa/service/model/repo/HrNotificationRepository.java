package com.pangreksa.service.model.repo;

import com.pangreksa.service.model.entity.HrNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface HrNotificationRepository extends JpaRepository<HrNotification, Long> {

    List<HrNotification> findByUsernameAndCreatedAtAfterOrderByCreatedAtDesc(
            String username, LocalDateTime after);

    int countByUsernameAndIsReadFalse(String username);
}