package com.pangreksa.service.service;

import com.pangreksa.service.model.entity.HrNotification;
import com.pangreksa.service.model.repo.HrNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private HrNotificationRepository repository;

    public List<HrNotification> getNotifications(String username) {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        return repository.findByUsernameAndCreatedAtAfterOrderByCreatedAtDesc(
                username, oneMonthAgo
        );
    }

    public int countUnread(String username) {
        return repository.countByUsernameAndIsReadFalse(username);
    }

    public void markAsRead(Long id) {
        HrNotification notif = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notif.setIsRead(true);
        repository.save(notif);
    }

    public void createNotification(String username, String title, String type, Long refId) {
        HrNotification notif = new HrNotification();
        notif.setUsername(username);
        notif.setTitle(title);
        notif.setType(type);
        notif.setReferenceId(refId);
        notif.setIsRead(false);
        notif.setCreatedAt(LocalDateTime.now());

        repository.save(notif);
    }
}