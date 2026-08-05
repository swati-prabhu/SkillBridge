package com.skillbridge.service;

import com.skillbridge.entity.Notification;
import com.skillbridge.entity.User;
import com.skillbridge.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void notify(User user, String message, String link) {
        Notification n = new Notification();
        n.setUser(user);
        n.setMessage(message);
        n.setLink(link);
        notificationRepository.save(n);
    }

    public List<Notification> recentForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public long unreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    public void markAllRead(User user) {
        List<Notification> all = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        all.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(all);
    }

    public void markRead(User user, Long notificationId) {
        notificationRepository.findById(notificationId)
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .ifPresent(n -> {
                    n.setRead(true);
                    notificationRepository.save(n);
                });
    }

    public void delete(User user, Long notificationId) {
        notificationRepository.findById(notificationId)
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .ifPresent(notificationRepository::delete);
    }
}
