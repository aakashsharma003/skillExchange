package com.oscc.skillexchange.repository;

import com.oscc.skillexchange.domain.entity.SessionFeedback;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionFeedbackRepository extends MongoRepository<SessionFeedback, String> {

    @Query("{ 'sessionUserId': ?0, 'skillExchanged': true }")
    List<SessionFeedback> findBySessionUserIdAndSkillExchangedTrue(String userId);

    @Query("{ 'scheduledSessionId': ?0 }")
    List<SessionFeedback> findByScheduledSessionId(String scheduledSessionId);

    @Query("{ 'sessionUserId': ?0 }")
    List<SessionFeedback> findBySessionUserId(String userId);
}
