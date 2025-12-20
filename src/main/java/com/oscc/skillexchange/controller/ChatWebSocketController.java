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
            ChatMessageResponse response = chatService.saveMessage(message);

            // 1. Send to the specific room (Matches ChatRoom.tsx subscription)
            messagingTemplate.convertAndSend("/topic/room/" + message.getChatRoomId(), response);

            // 2. Send to the personal user topic (Matches Sidebar/Chat.tsx subscription)
            messagingTemplate.convertAndSend("/topic/user/" + message.getReceiverId(), response);

        } catch (Exception e) {
            log.error("WebSocket error", e);
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
