package com.oscc.skillexchange.service;

import com.oscc.skillexchange.domain.entity.ChatMessage;
import com.oscc.skillexchange.domain.entity.ChatRoom;
import com.oscc.skillexchange.domain.entity.ExchangeRequest;
import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.dto.response.ChatMessageResponse;
import com.oscc.skillexchange.dto.response.ChatRoomResponse;
import com.oscc.skillexchange.exception.ResourceNotFoundException;
import com.oscc.skillexchange.repository.ChatMessageRepository;
import com.oscc.skillexchange.repository.ChatRoomRepository;
import com.oscc.skillexchange.repository.ExchangeRequestRepository;
import com.oscc.skillexchange.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final UserService userService;
    private final EntityMapper mapper;

    /**
     * Create or get existing chat room between two users
     */
    @Transactional
    public ChatRoom createOrGetChatRoom(String userId1, String userId2, String exchangeRequestId) {
        log.info("Creating/getting chat room between {} and {}", userId1, userId2);

        return chatRoomRepository.findByParticipants(userId1, userId2)
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.builder()
                            .senderId(userId1)
                            .receiverId(userId2)
                            .exchangeRequestId(exchangeRequestId)
                            .lastActivityAt(Instant.now())
                            .build();

                    ChatRoom saved = chatRoomRepository.save(newRoom);
                    log.info("New chat room created: {}", saved.getId());
                    return saved;
                });
    }

    /**
     * Get chat room by ID
     */
    public ChatRoom getChatRoomById(String roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room", roomId));
    }

    /**
     * Get all chat rooms for a user
     */
    public List<ChatRoomResponse> getUserChatRooms(String userId) {
        log.info("Fetching chat rooms for user: {}", userId);

        List<ChatRoom> rooms = chatRoomRepository.findByUserIdOrderByLastActivityAtDesc(userId);

        return rooms.stream()
                .map(room -> {
                    // Determine the other user
                    String otherUserId = room.getSenderId().equals(userId)
                            ? room.getReceiverId()
                            : room.getSenderId();

                    User otherUser = userService.getUserById(otherUserId);
                    ChatRoomResponse resp = mapper.toChatRoomResponse(room, otherUser);
                    // Provide frontend-friendly fields
                    resp.setChatRoomId(room.getId());

                    if (room.getExchangeRequestId() != null) {
                        exchangeRequestRepository.findById(room.getExchangeRequestId()).ifPresent(req -> {
                            resp.setOfferedSkill(req.getOfferedSkill());
                            resp.setRequestedSkill(req.getRequestedSkill());
                        });
                    }

                    return resp;
                })
                .sorted(Comparator.comparing(ChatRoomResponse::getLastActivityAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Save chat message
     */
    @Transactional
    public ChatMessageResponse saveMessage(ChatMessage message) {
        log.info("Saving message to room: {}", message.getChatRoomId());

        // Validate chat room exists
        ChatRoom room = getChatRoomById(message.getChatRoomId());

        // Set timestamp if not set
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(Instant.now());
        }

        // Save message
        message = chatMessageRepository.save(message);

        // Update room's last activity
        room.setLastActivityAt(Instant.now());
        chatRoomRepository.save(room);

        log.info("Message saved successfully: {}", message.getId());

        return mapper.toChatMessageResponse(message);
    }

    /**
     * Get messages for a chat room
     */
    public List<ChatMessageResponse> getChatMessages(String roomId) {
        log.info("Fetching messages for room: {}", roomId);

        // Validate room exists
        getChatRoomById(roomId);

        List<ChatMessage> messages = chatMessageRepository
                .findByChatRoomIdOrderByCreatedAtAsc(roomId);

        return mapper.toChatMessageResponseList(messages);
    }

    /**
     * Get recent messages after a timestamp
     */
    public List<ChatMessageResponse> getMessagesAfter(String roomId, Instant after) {
        log.info("Fetching messages for room {} after {}", roomId, after);

        List<ChatMessage> messages = chatMessageRepository
                .findByChatRoomIdAndCreatedAtAfter(roomId, after);

        return mapper.toChatMessageResponseList(messages);
    }

    /**
     * Check if user has access to chat room
     */
    public boolean hasAccessToChatRoom(String userId, String roomId) {
        ChatRoom room = getChatRoomById(roomId);
        return room.getSenderId().equals(userId) || room.getReceiverId().equals(userId);
    }
}
