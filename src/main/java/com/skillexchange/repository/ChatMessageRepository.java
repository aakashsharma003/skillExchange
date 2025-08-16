package com.skillexchange.repository;

import com.skillexchange.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    // ChatMessage has a UUID field named `chatRoomId`
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(UUID chatRoomId);
}
