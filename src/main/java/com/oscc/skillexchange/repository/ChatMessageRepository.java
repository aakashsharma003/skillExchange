package com.oscc.skillexchange.repository;

import com.oscc.skillexchange.domain.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;


@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(String chatRoomId);

    @Query(value = "{ 'chatRoomId': ?0 }",
            sort = "{ 'createdAt': -1 }")
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(String chatRoomId);

    @Query(value = "{ 'chatRoomId': ?0, 'createdAt': { $gt: ?1 } }",
            sort = "{ 'createdAt': 1 }")
    List<ChatMessage> findByChatRoomIdAndCreatedAtAfter(String chatRoomId, Instant after);
}
