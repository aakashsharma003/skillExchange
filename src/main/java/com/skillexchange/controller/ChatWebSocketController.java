package com.skillexchange.controller;

import com.skillexchange.model.ChatMessage;
import com.skillexchange.model.ChatRoom;
import com.skillexchange.repository.ChatRoomRepository;
import com.skillexchange.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final ChatRoomRepository chatRoomRepository;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate,
                                   ChatService chatService,
                                   ChatRoomRepository chatRoomRepository) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.chatRoomRepository = chatRoomRepository;
    }

    @MessageMapping("/chat.sendMessage") // clients send to /app/chat.sendMessage
    public void sendMessage(@Payload ChatMessage message) {
        // Expect ChatMessage.chatRoomId as UUID (Jackson can bind from a UUID string)
        UUID roomId = message.getChatRoomId();

        if (roomId != null) {
            Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findById(roomId);
            if (chatRoomOpt.isPresent()) {
                ChatRoom room = chatRoomOpt.get();
                room.setLastActivityAt(LocalDateTime.now());
                chatRoomRepository.save(room);
            }
        }

        // Ensure createdAt present (DB also has DEFAULT now(); this is just defensive)
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(LocalDateTime.now());
        }

        // Persist the message
        chatService.saveMessages(message);

        // Fan out to room topic
        String topicRoomId = (roomId != null) ? roomId.toString() : "unknown";
        messagingTemplate.convertAndSend("/topic/messages/" + topicRoomId, message);

        // Also send to receiver-specific topic if provided
        UUID receiverId = message.getReceiverId(); // keep this field in your ChatMessage model
        if (receiverId != null) {
            messagingTemplate.convertAndSend("/topic/user/" + receiverId, message);
        }
    }
}
