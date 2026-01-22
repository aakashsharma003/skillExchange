package com.oscc.skillexchange.controller;

import com.oscc.skillexchange.domain.entity.ChatMessage;
import com.oscc.skillexchange.dto.response.ChatMessageResponse;
import com.oscc.skillexchange.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.time.Instant;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle incoming chat messages via WebSocket
     * Clients send to: /app/chat.send
     */
    @MessageMapping("/chat.send") // Matches the frontend destination
    public void sendMessage(@Payload ChatMessage message) {
        try {
            log.info("Received WebSocket message: roomId={}, senderId={}, receiverId={}, content={}", 
                    message.getChatRoomId(), message.getSenderId(), message.getReceiverId(), 
                    message.getContent() != null ? message.getContent().substring(0, Math.min(50, message.getContent().length())) : "null");
            
            // Validate required fields
            if (message.getChatRoomId() == null || message.getChatRoomId().isBlank()) {
                log.error("Chat room ID is missing in message");
                return;
            }
            
            if (message.getSenderId() == null || message.getSenderId().isBlank()) {
                log.error("Sender ID is missing in message");
                return;
            }
            
            if (message.getContent() == null || message.getContent().isBlank()) {
                log.error("Message content is missing");
                return;
            }

            ChatMessageResponse response = chatService.saveMessage(message);
            log.info("Message saved successfully: messageId={}, roomId={}", response.getId(), response.getChatRoomId());

            // 1. Send to the specific room (Matches ChatRoom.tsx subscription)
            messagingTemplate.convertAndSend("/topic/room/" + message.getChatRoomId(), response);
            log.debug("Message sent to room topic: /topic/room/{}", message.getChatRoomId());

            // 2. Send to the personal user topic (Matches Sidebar/Chat.tsx subscription)
            if (message.getReceiverId() != null && !message.getReceiverId().isBlank()) {
                messagingTemplate.convertAndSend("/topic/user/" + message.getReceiverId(), response);
                log.debug("Message sent to user topic: /topic/user/{}", message.getReceiverId());
            }

        } catch (Exception e) {
            log.error("WebSocket error while processing message: roomId={}, senderId={}", 
                    message.getChatRoomId(), message.getSenderId(), e);
        }
    }

    /**
     * Handle typing indicator
     * Clients send to: /app/chat.typing/{roomId}
     */
    @MessageMapping("/chat.typing/{roomId}")
    public void handleTypingIndicator(
            @DestinationVariable String roomId,
            @Payload String userId) {
        log.debug("User {} is typing in room {}", userId, roomId);
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/typing",
                userId
        );
    }
}
