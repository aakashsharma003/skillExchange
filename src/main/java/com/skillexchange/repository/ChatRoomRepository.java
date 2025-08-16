package com.skillexchange.repository;

import com.skillexchange.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    // Updated to match ChatRoom entity fields: senderId and receiverId
    List<ChatRoom> findBySenderIdOrReceiverId(UUID senderId, UUID receiverId);
}
