package com.skillexchange.service;

import com.skillexchange.model.ChatMessage;
import com.skillexchange.model.ChatRoom;
import com.skillexchange.repository.ChatMessageRepository;
import com.skillexchange.repository.ChatRoomRepository;
import com.skillexchange.repository.ExchangeRequestRepository;
import com.skillexchange.repository.AuthRepository;
import com.skillexchange.model.dto.ChatThreadDTO;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.UUID;

@Service
public class ChatService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private ExchangeRequestRepository exchangeRequestRepository;
    @Autowired
    private AuthRepository authRepository;

    /* ---------- rooms ---------- */

    public void createChatRoom(ChatRoom chatroom) {
        chatRoomRepository.save(chatroom);
    }

    public Optional<ChatRoom> findById(String chatRoomId) {
        return parseUUID(chatRoomId).flatMap(chatRoomRepository::findById);
    }

    /* ---------- messages ---------- */

    public List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(String chatRoomId) {
        return parseUUID(chatRoomId)
            .map(chatMessageRepository::findByChatRoomIdOrderByCreatedAtAsc)
            .orElseGet(List::of);
    }

    public void saveMessages(ChatMessage chatMessage) {
        chatMessageRepository.save(chatMessage);
    }

    /* ---------- threads for a user ---------- */

    public List<ChatThreadDTO> getUserChatRooms(String userId, String email) {
        UUID uid = parseUUID(userId).orElse(null);
        if (uid == null) return Collections.emptyList();

        List<ChatRoom> rooms = chatRoomRepository.findBySenderIdOrReceiverId(uid, uid);
        rooms.sort(Comparator.comparing(ChatRoom::getLastActivityAt).reversed());

        List<ChatThreadDTO> chatThreads = new ArrayList<>();
        for (ChatRoom room : rooms) {
            ChatThreadDTO dto = new ChatThreadDTO();
            dto.setChatRoomId(room.getId());

            UUID otherUserId = (room.getSenderId() != null && !room.getSenderId().equals(uid))
                               ? room.getSenderId()
                               : room.getReceiverId();

            authRepository.findById(otherUserId).ifPresent(dto::setUser);

            chatThreads.add(dto);
        }

        return chatThreads;
    }

    /* ---------- helpers ---------- */

    private static Optional<UUID> parseUUID(String s) {
        try {
            return (s == null || s.isBlank()) ? Optional.empty() : Optional.of(UUID.fromString(s));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
