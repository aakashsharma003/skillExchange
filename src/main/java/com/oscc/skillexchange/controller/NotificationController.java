package com.oscc.skillexchange.controller;

import com.oscc.skillexchange.dto.response.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Notification-Service Controller
 * Handles STOMP mappings and sends notifications to specific user topics
 * Uses /user/{userId}/queue/notifications for user-specific notifications
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final com.oscc.skillexchange.service.NotificationService notificationService;

    /**
     * Send notification to a specific user
     * POST /api/notifications/send
     */
    @PostMapping("/send")
    public void sendNotification(@RequestBody NotificationDto notification) {
        log.info("Sending notification to user: {}", notification.getUserId());
        notificationService.sendNotification(notification);
    }

    /**
     * STOMP endpoint for subscribing to notifications
     * Clients subscribe to: /user/{userId}/queue/notifications
     */
    @MessageMapping("/notifications")
    @SendTo("/user/{userId}/queue/notifications")
    public NotificationDto handleNotification(@Payload NotificationDto notification) {
        log.info("Handling notification: {}", notification);
        return notification;
    }
}
