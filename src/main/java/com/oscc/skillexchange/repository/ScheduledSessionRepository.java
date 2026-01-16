package com.oscc.skillexchange.repository;

import com.oscc.skillexchange.domain.entity.ScheduledSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledSessionRepository extends MongoRepository<ScheduledSession, String> {

    @Query("{ $or: [ { 'userId1': ?0 }, { 'userId2': ?0 } ] }")
    List<ScheduledSession> findByUserId(String userId);

    @Query("{ $or: [ { 'userId1': ?0 }, { 'userId2': ?0 } ], 'status': ?1 }")
    List<ScheduledSession> findByUserIdAndStatus(String userId, ScheduledSession.SessionStatus status);

    @Query("{ $or: [ { 'userId1': ?0 }, { 'userId2': ?0 } ], 'sessionDate': ?1, 'status': { $in: ?2 } }")
    List<ScheduledSession> findByUserIdAndSessionDateAndStatusIn(
            String userId,
            Instant sessionDate,
            List<ScheduledSession.SessionStatus> statuses
    );

    @Query("{ 'chatRoomId': ?0 }")
    List<ScheduledSession> findByChatRoomId(String chatRoomId);
}
