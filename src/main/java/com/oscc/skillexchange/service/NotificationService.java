package com.oscc.skillexchange.service;

import com.oscc.skillexchange.dto.response.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Notification Service for sending WebSocket notifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send match notification
     */
    public void sendMatchNotification(String userId, String matchedUserName, String skill) {
        log.info("Sending match notification to user: {} for match with: {}", userId, matchedUserName);

        NotificationDto notification = NotificationDto.builder()
                .type("MATCH")
                .title("New Match!")
                .message(String.format("%s can teach you %s", matchedUserName, skill))
                .userId(userId)
                .timestamp(System.currentTimeMillis())
                .build();

        String destination = "/user/" + userId + "/queue/notifications";
        messagingTemplate.convertAndSend(destination, notification);
    }

    /**
     * Send generic notification
     */
    public void sendNotification(NotificationDto notification) {
        String destination = "/user/" + notification.getUserId() + "/queue/notifications";
        messagingTemplate.convertAndSend(destination, notification);
    }
}
