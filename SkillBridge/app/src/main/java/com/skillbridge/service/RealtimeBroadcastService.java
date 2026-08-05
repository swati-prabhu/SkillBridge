package com.skillbridge.service;

import com.skillbridge.dto.ApplicationStatusEvent;
import com.skillbridge.entity.Application;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Publishes application status changes over WebSocket/STOMP so dashboards
 * update live instead of requiring a page refresh. Broadcasting is
 * best-effort: if messaging fails for any reason, it's caught and logged
 * rather than allowed to break the actual status-update transaction.
 */
@Service
@RequiredArgsConstructor
public class RealtimeBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastStatusChange(Application application) {
        try {
            ApplicationStatusEvent event = new ApplicationStatusEvent(
                    application.getId(),
                    application.getStudent().getId(),
                    application.getInternship().getTitle(),
                    application.getInternship().getCompany(),
                    application.getStatus().name(),
                    application.getStudent().getFullName() + "'s application for \"" + application.getInternship().getTitle() + "\" is now " + application.getStatus().name()
            );
            messagingTemplate.convertAndSend("/topic/applications", event);
            messagingTemplate.convertAndSend("/topic/applications/" + application.getStudent().getId(), event);
        } catch (Exception ex) {
            // Real-time updates are a nice-to-have; never let a broadcast failure break the actual status update.
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("Failed to broadcast application status event: {}", ex.getMessage());
        }
    }
}
