package com.oscc.skillexchange.repository;

import com.oscc.skillexchange.domain.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    @Query("{ $or: [ { 'senderId': ?0 }, { 'receiverId': ?0 } ] }")
    List<ChatRoom> findByUserIdOrderByLastActivityAtDesc(String userId);

    @Query("{ $or: [ " +
            "{ 'senderId': ?0, 'receiverId': ?1 }, " +
            "{ 'senderId': ?1, 'receiverId': ?0 } " +
            "] }")
    Optional<ChatRoom> findByParticipants(String userId1, String userId2);

    boolean existsByExchangeRequestId(String exchangeRequestId);
}
